package com.tradingbot.backend.controller;

import com.tradingbot.backend.repo.TradeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
// trade controller handles retrieval of trade information
// it includes an endpoint to get trades by account ID
@RestController
@RequestMapping("/api")

public class TradeController {

    private final TradeRepository tradeRepo;

    public TradeController(TradeRepository tradeRepo) {
        this.tradeRepo = tradeRepo;
    }

    @GetMapping("/trades")
    public List<Map<String, Object>> getTrades(@RequestParam long accountId) {
        return tradeRepo.getTrades(accountId);
    }
}
