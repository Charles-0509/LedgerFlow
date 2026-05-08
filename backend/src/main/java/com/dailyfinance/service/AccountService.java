package com.dailyfinance.service;

import com.dailyfinance.common.BusinessException;
import com.dailyfinance.dto.AccountRequest;
import com.dailyfinance.mapper.AccountMapper;
import com.dailyfinance.model.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {
    private final AccountMapper accountMapper;

    public AccountService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    public List<Account> list(Long userId) {
        return accountMapper.listByUser(userId);
    }

    @Transactional
    public Account create(Long userId, AccountRequest request) {
        Account account = new Account();
        account.userId = userId;
        fill(account, request);
        if (account.isDefault == 1) {
            accountMapper.clearDefault(userId);
        }
        accountMapper.insert(account);
        return account;
    }

    @Transactional
    public Account update(Long userId, Long id, AccountRequest request) {
        Account account = accountMapper.findById(id, userId);
        if (account == null) {
            throw new BusinessException("账户不存在");
        }
        fill(account, request);
        if (account.isDefault == 1) {
            accountMapper.clearDefault(userId);
        }
        accountMapper.update(account);
        return accountMapper.findById(id, userId);
    }

    public void delete(Long userId, Long id) {
        if (accountMapper.softDelete(id, userId) == 0) {
            throw new BusinessException("账户不存在");
        }
    }

    private void fill(Account account, AccountRequest request) {
        account.name = request.name.trim();
        account.type = request.type == null || request.type.isBlank() ? "CASH" : request.type.trim();
        account.balance = request.balance == null ? BigDecimal.ZERO : request.balance;
        account.isDefault = Boolean.TRUE.equals(request.isDefault) ? 1 : 0;
    }
}
