package com.dailyfinance.service;

import com.dailyfinance.common.BusinessException;
import com.dailyfinance.dto.CategoryRequest;
import com.dailyfinance.mapper.CategoryMapper;
import com.dailyfinance.model.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> list(Long userId, String type) {
        return categoryMapper.list(userId, normalizeTypeNullable(type));
    }

    public Category create(Long userId, CategoryRequest request) {
        Category category = new Category();
        category.userId = userId;
        fill(category, request);
        category.isSystem = 0;
        categoryMapper.insert(category);
        return category;
    }

    public Category update(Long userId, Long id, CategoryRequest request) {
        Category category = categoryMapper.findById(id, userId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        fill(category, request);
        categoryMapper.update(category);
        return categoryMapper.findById(id, userId);
    }

    public void delete(Long userId, Long id) {
        if (categoryMapper.softDelete(id, userId) == 0) {
            throw new BusinessException("分类不存在");
        }
    }

    private void fill(Category category, CategoryRequest request) {
        category.name = request.name.trim();
        category.type = normalizeType(request.type);
        category.icon = request.icon == null || request.icon.isBlank() ? null : request.icon.trim();
        category.color = request.color == null || request.color.isBlank() ? "#409eff" : request.color.trim();
    }

    private String normalizeTypeNullable(String type) {
        return type == null || type.isBlank() ? null : normalizeType(type);
    }

    private String normalizeType(String type) {
        String value = type == null ? "" : type.trim().toUpperCase();
        if (!"INCOME".equals(value) && !"EXPENSE".equals(value)) {
            throw new BusinessException("分类类型必须为 INCOME 或 EXPENSE");
        }
        return value;
    }
}
