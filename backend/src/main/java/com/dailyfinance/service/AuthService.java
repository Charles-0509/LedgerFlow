package com.dailyfinance.service;

import com.dailyfinance.common.BusinessException;
import com.dailyfinance.dto.AuthResponse;
import com.dailyfinance.dto.LoginRequest;
import com.dailyfinance.dto.RegisterRequest;
import com.dailyfinance.dto.UserInfo;
import com.dailyfinance.mapper.AccountMapper;
import com.dailyfinance.mapper.CategoryMapper;
import com.dailyfinance.mapper.UserMapper;
import com.dailyfinance.mapper.UserProfileMapper;
import com.dailyfinance.model.Account;
import com.dailyfinance.model.Category;
import com.dailyfinance.model.User;
import com.dailyfinance.security.PasswordService;
import com.dailyfinance.security.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final UserProfileMapper profileMapper;
    private final AccountMapper accountMapper;
    private final CategoryMapper categoryMapper;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public AuthService(UserMapper userMapper,
                       UserProfileMapper profileMapper,
                       AccountMapper accountMapper,
                       CategoryMapper categoryMapper,
                       PasswordService passwordService,
                       TokenService tokenService) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.accountMapper = accountMapper;
        this.categoryMapper = categoryMapper;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username.trim();
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.username = username;
        user.passwordHash = passwordService.encode(request.password);
        user.nickname = blankToDefault(request.nickname, username);
        user.status = 1;
        userMapper.insert(user);

        Account account = new Account();
        account.userId = user.id;
        account.name = "现金账户";
        account.type = "CASH";
        account.balance = BigDecimal.ZERO;
        account.isDefault = 1;
        accountMapper.insert(account);

        profileMapper.insert(user.id, new BigDecimal("3000.00"), "CNY", account.id);
        createDefaultCategories(user.id);

        String token = tokenService.createToken(user.id);
        return new AuthResponse(token, new UserInfo(user.id, user.username, user.nickname));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.username.trim());
        if (user == null || user.status == null || user.status != 1) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordService.matches(request.password, user.passwordHash)) {
            throw new BusinessException("用户名或密码错误");
        }
        String token = tokenService.createToken(user.id);
        return new AuthResponse(token, new UserInfo(user.id, user.username, user.nickname));
    }

    public void logout(String authorization) {
        if (authorization != null) {
            tokenService.revoke(authorization.replaceFirst("(?i)^Bearer\\s+", "").trim());
        }
    }

    private void createDefaultCategories(Long userId) {
        List<Category> categories = List.of(
                category(userId, "工资", "INCOME", "Wallet", "#2f855a"),
                category(userId, "奖金", "INCOME", "Money", "#38a169"),
                category(userId, "兼职", "INCOME", "Briefcase", "#319795"),
                category(userId, "理财收益", "INCOME", "TrendCharts", "#3182ce"),
                category(userId, "餐饮", "EXPENSE", "Bowl", "#e53e3e"),
                category(userId, "交通", "EXPENSE", "Van", "#dd6b20"),
                category(userId, "购物", "EXPENSE", "ShoppingCart", "#805ad5"),
                category(userId, "学习", "EXPENSE", "Reading", "#2b6cb0"),
                category(userId, "医疗", "EXPENSE", "FirstAidKit", "#c53030"),
                category(userId, "娱乐", "EXPENSE", "VideoPlay", "#d69e2e"),
                category(userId, "住房", "EXPENSE", "House", "#4a5568")
        );
        categories.forEach(categoryMapper::insert);
    }

    private Category category(Long userId, String name, String type, String icon, String color) {
        Category category = new Category();
        category.userId = userId;
        category.name = name;
        category.type = type;
        category.icon = icon;
        category.color = color;
        category.isSystem = 1;
        return category;
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
