package com.tradingbot.backend.controller;

import com.tradingbot.backend.service.BotService;
import com.tradingbot.backend.service.MACrossoverStrategy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BotController {

    private final BotService botService;

    public BotController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    // ===== TRADING MODE =====
@PostMapping("/step")
public MACrossoverStrategy.Signal step(@RequestParam long accountId,
                                      @RequestParam String symbol) {
    return botService.runTradingStep(accountId, symbol);
}

@PostMapping("/trade/step")
public MACrossoverStrategy.Signal tradeStep(@RequestParam long accountId,
                                            @RequestParam String symbol) {
    return botService.runTradingStep(accountId, symbol);
}


    // ===== TRAINING MODE (batch) =====
    @PostMapping("/train")
    public String train(@RequestParam long accountId,
                        @RequestParam String symbol,
                        @RequestParam(defaultValue = "200") int limit,
                        @RequestParam(defaultValue = "0") int offset) {
        int trades = botService.runTraining(accountId, symbol, limit, offset);
        return "Training done. Trades executed=" + trades;
    }

    // ===== TRAINING MODE (step) =====
    @PostMapping("/train/step")
    public BotService.TrainingStepResult trainStep(@RequestParam long accountId,
                                                   @RequestParam String symbol,
                                                   @RequestParam(defaultValue = "200") int limit,
                                                   @RequestParam int index,
                                                   @RequestParam(defaultValue = "0") int offset,
                                                   @RequestBody(required = false) List<BotService.CandleDTO> candles) {

        if (candles != null && !candles.isEmpty()) {
            return botService.runTrainingStepWithCandles(accountId, symbol, candles, index);
        }

        return botService.runTrainingStep(accountId, symbol, limit, index, offset);
    }
}
