package com.tradingbot.backend.service;

import com.tradingbot.backend.repo.AccountRepository;
import com.tradingbot.backend.repo.EquitySnapshotRepository;
import com.tradingbot.backend.repo.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class EquityService {

    private final AccountRepository accountRepo;
    private final PortfolioRepository portfolioRepo;
    private final EquitySnapshotRepository snapshotRepo;
    private final BinancePriceService priceService;

    public EquityService(AccountRepository accountRepo,
                         PortfolioRepository portfolioRepo,
                         EquitySnapshotRepository snapshotRepo,
                         BinancePriceService priceService) {
        this.accountRepo = accountRepo;
        this.portfolioRepo = portfolioRepo;
        this.snapshotRepo = snapshotRepo;
        this.priceService = priceService;
    }

    @Transactional
    public void snapshot(long accountId, String mode) {
        BigDecimal cash = accountRepo.getCashBalance(accountId);

        List<Map<String, Object>> positions = portfolioRepo.getPortfolio(accountId);

        BigDecimal portfolioValue = BigDecimal.ZERO;

        for (Map<String, Object> pos : positions) {
            String symbol = (String) pos.get("symbol");
            BigDecimal qty = (BigDecimal) pos.get("quantity");
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal latestPrice = priceService.getLatestPrice(symbol);
            portfolioValue = portfolioValue.add(qty.multiply(latestPrice));
        }

        portfolioValue = portfolioValue.setScale(8, RoundingMode.HALF_UP);
        BigDecimal totalEquity = cash.add(portfolioValue).setScale(8, RoundingMode.HALF_UP);

        snapshotRepo.insertSnapshot(accountId, mode, cash, portfolioValue, totalEquity);
    }

    public List<Map<String, Object>> getSnapshots(long accountId, String mode, int limit) {
        return snapshotRepo.getSnapshots(accountId, mode, limit);
    }
}
