package com.tradingbot.backend.controller;

import com.tradingbot.backend.service.ResetService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ResetController {

    private final ResetService resetService;

    public ResetController(ResetService resetService) {
        this.resetService = resetService;
    }

    @PostMapping("/reset")
    public String reset(@RequestParam long accountId) {
        resetService.resetAccount(accountId);
        return "OK";
    }
}
