package com.dailyfinance.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    public String name;

    @NotBlank(message = "分类类型不能为空")
    public String type;

    public String icon;
    public String color;
}
