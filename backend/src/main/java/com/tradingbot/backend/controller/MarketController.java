package com.tradingbot.backend.controller;

import com.tradingbot.backend.service.BinancePriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final BinancePriceService priceService;

    public MarketController(BinancePriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/candles")
    public List<BinancePriceService.Candle> candles(@RequestParam String symbol,
                                                    @RequestParam(defaultValue = "1m") String interval,
                                                    @RequestParam(defaultValue = "100") int limit) {
        return priceService.getCandles(symbol, interval, limit);
    }
}
