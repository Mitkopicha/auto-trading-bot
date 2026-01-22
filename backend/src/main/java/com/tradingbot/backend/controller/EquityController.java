package com.tradingbot.backend.controller;
// equity controller handles equity snapshot endpoints 
// and retrieval of equity snapshots for accounts 
import com.tradingbot.backend.service.EquityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equity")
public class EquityController {

    private final EquityService equityService;

    public EquityController(EquityService equityService) {
        this.equityService = equityService;
    }

    @PostMapping("/snapshot")
    public String snapshot(@RequestParam long accountId,
                           @RequestParam String mode) {
        equityService.snapshot(accountId, mode);
        return "OK";
    }

    @GetMapping("/snapshots")
    public List<Map<String, Object>> snapshots(@RequestParam long accountId,
                                               @RequestParam String mode,
                                               @RequestParam(defaultValue = "200") int limit) {
        return equityService.getSnapshots(accountId, mode, limit);
    }
}
