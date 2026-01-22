package com.tradingbot.backend.controller;
// account controller handles retrieval of account information
// it includes an endpoint to get account details by account ID
import com.tradingbot.backend.repo.AccountRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")

public class AccountController {

    private final AccountRepository accountRepo;

    public AccountController(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    @GetMapping("/account")
    public Map<String, Object> getAccount(@RequestParam long accountId) {
        return accountRepo.getAccount(accountId);
    }
}
