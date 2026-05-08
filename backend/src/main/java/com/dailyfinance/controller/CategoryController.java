package com.dailyfinance.controller;

import com.dailyfinance.common.ApiResponse;
import com.dailyfinance.common.UserContext;
import com.dailyfinance.dto.CategoryRequest;
import com.dailyfinance.model.Category;
import com.dailyfinance.model.RecordView;
import com.dailyfinance.service.CategoryService;
import com.dailyfinance.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final RecordService recordService;

    public CategoryController(CategoryService categoryService, RecordService recordService) {
        this.categoryService = categoryService;
        this.recordService = recordService;
    }

    @GetMapping
    public ApiResponse<List<Category>> list(@RequestParam(required = false) String type) {
        return ApiResponse.ok(categoryService.list(UserContext.requireUserId(), type));
    }

    @PostMapping
    public ApiResponse<Category> create(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok(categoryService.create(UserContext.requireUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok(categoryService.update(UserContext.requireUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(UserContext.requireUserId(), id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/records")
    public ApiResponse<List<RecordView>> records(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.ok(recordService.search(UserContext.requireUserId(), null, id, null, null, null, page, size).records);
    }
}
