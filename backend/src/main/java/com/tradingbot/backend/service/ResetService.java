package com.tradingbot.backend.service;

import com.tradingbot.backend.repo.AccountRepository;
import com.tradingbot.backend.repo.EquitySnapshotRepository;
import com.tradingbot.backend.repo.PortfolioRepository;
import com.tradingbot.backend.repo.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ResetService {

    private final TradeRepository tradeRepo;
    private final PortfolioRepository portfolioRepo;
    private final EquitySnapshotRepository snapshotRepo;
    private final AccountRepository accountRepo;

    public ResetService(TradeRepository tradeRepo,
                        PortfolioRepository portfolioRepo,
                        EquitySnapshotRepository snapshotRepo,
                        AccountRepository accountRepo) {
        this.tradeRepo = tradeRepo;
        this.portfolioRepo = portfolioRepo;
        this.snapshotRepo = snapshotRepo;
        this.accountRepo = accountRepo;
    }

    @Transactional
    public void resetAccount(long accountId) {
        tradeRepo.deleteTradesForAccount(accountId);
        portfolioRepo.deletePortfolioForAccount(accountId);
        snapshotRepo.deleteSnapshotsForAccount(accountId);
        accountRepo.resetCash(accountId, new BigDecimal("10000.00000000"));
    }
}
