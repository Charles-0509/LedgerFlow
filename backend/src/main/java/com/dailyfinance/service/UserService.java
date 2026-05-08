package com.dailyfinance.service;

import com.dailyfinance.common.BusinessException;
import com.dailyfinance.dto.ProfileRequest;
import com.dailyfinance.mapper.AccountMapper;
import com.dailyfinance.mapper.UserMapper;
import com.dailyfinance.mapper.UserProfileMapper;
import com.dailyfinance.model.Account;
import com.dailyfinance.model.User;
import com.dailyfinance.model.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final UserProfileMapper profileMapper;
    private final AccountMapper accountMapper;

    public UserService(UserMapper userMapper, UserProfileMapper profileMapper, AccountMapper accountMapper) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.accountMapper = accountMapper;
    }

    public Map<String, Object> getProfile(Long userId) {
        User user = userMapper.findById(userId);
        UserProfile profile = ensureProfile(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", user);
        result.put("profile", profile);
        result.put("accounts", accountMapper.listByUser(userId));
        return result;
    }

    @Transactional
    public Map<String, Object> updateProfile(Long userId, ProfileRequest request) {
        if (request.defaultAccountId != null) {
            Account account = accountMapper.findById(request.defaultAccountId, userId);
            if (account == null) {
                throw new BusinessException("默认账户不存在");
            }
        }
        UserProfile profile = ensureProfile(userId);
        profile.defaultMonthlyBudget = request.defaultMonthlyBudget == null ? BigDecimal.ZERO : request.defaultMonthlyBudget;
        profile.currency = request.currency == null || request.currency.isBlank() ? "CNY" : request.currency.trim();
        profile.defaultAccountId = request.defaultAccountId;
        profileMapper.update(profile);

        if (request.nickname != null && !request.nickname.isBlank()) {
            userMapper.updateNickname(userId, request.nickname.trim());
        }
        return getProfile(userId);
    }

    private UserProfile ensureProfile(Long userId) {
        UserProfile profile = profileMapper.findByUserId(userId);
        if (profile != null) {
            return profile;
        }
        Account account = accountMapper.findPreferred(userId);
        Long accountId = account == null ? null : account.id;
        profileMapper.insert(userId, new BigDecimal("3000.00"), "CNY", accountId);
        return profileMapper.findByUserId(userId);
    }
}
