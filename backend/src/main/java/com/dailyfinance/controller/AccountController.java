package com.dailyfinance.controller;

import com.dailyfinance.common.ApiResponse;
import com.dailyfinance.common.UserContext;
import com.dailyfinance.dto.AccountRequest;
import com.dailyfinance.model.Account;
import com.dailyfinance.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ApiResponse<List<Account>> list() {
        return ApiResponse.ok(accountService.list(UserContext.requireUserId()));
    }

    @PostMapping
    public ApiResponse<Account> create(@Valid @RequestBody AccountRequest request) {
        return ApiResponse.ok(accountService.create(UserContext.requireUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Account> update(@PathVariable Long id, @Valid @RequestBody AccountRequest request) {
        return ApiResponse.ok(accountService.update(UserContext.requireUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        accountService.delete(UserContext.requireUserId(), id);
        return ApiResponse.ok();
    }
}
