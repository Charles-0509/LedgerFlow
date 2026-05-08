package com.dailyfinance.controller;

import com.dailyfinance.common.ApiResponse;
import com.dailyfinance.common.PageResult;
import com.dailyfinance.common.UserContext;
import com.dailyfinance.dto.RecordRequest;
import com.dailyfinance.model.RecordView;
import com.dailyfinance.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
public class RecordController {
    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public ApiResponse<PageResult<RecordView>> search(@RequestParam(required = false) String type,
                                                      @RequestParam(required = false) Long categoryId,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(recordService.search(UserContext.requireUserId(), type, categoryId, startDate, endDate, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<RecordView> detail(@PathVariable Long id) {
        return ApiResponse.ok(recordService.detail(UserContext.requireUserId(), id));
    }

    @PostMapping
    public ApiResponse<RecordView> create(@Valid @RequestBody RecordRequest request) {
        return ApiResponse.ok(recordService.create(UserContext.requireUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RecordView> update(@PathVariable Long id, @Valid @RequestBody RecordRequest request) {
        return ApiResponse.ok(recordService.update(UserContext.requireUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        recordService.delete(UserContext.requireUserId(), id);
        return ApiResponse.ok();
    }
}
