package com.tradingbot.backend.controller;
// market controller handles retrieval of market data such as candlestick information
// it includes an endpoint to get candlestick data for a given symbol and interval
// using the BinancePriceService
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
    public List<java.util.Map<String, Object>> candles(@RequestParam String symbol,
                                                      @RequestParam(defaultValue = "1m") String interval,
                                                      @RequestParam(defaultValue = "100") int limit,
                                                      @RequestParam(defaultValue = "0") int offset) {
        List<BinancePriceService.Candle> candles = priceService.getCandles(symbol, interval, limit, offset);
        return candles.stream().map(c -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("timestamp", c.timestamp);
            map.put("close", c.close);
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }
}
