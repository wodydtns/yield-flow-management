package com.yieldflow.management.domain.account.service;

import com.yieldflow.management.domain.account.dto.AccountTransactionResponse;
import com.yieldflow.management.domain.account.entity.AccountTransaction;
import com.yieldflow.management.domain.account.repository.AccountTransactionRepository;
import com.yieldflow.management.global.client.BithumbDepositWithdrawalClient;
import com.yieldflow.management.global.client.dto.BithumbDepositWithdrawalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 계좌 입출금 거래 내역 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountTransactionService {

    private final AccountTransactionRepository accountTransactionRepository;
    private final BithumbDepositWithdrawalClient bithumbClient;

    /**
     * 빗썸 API에서 입출금 현황을 조회하고 DB에 동기화
     */
    @Transactional
    public AccountTransactionResponse.AccountTransactionSummaryResponse syncAndGetTransactions(
            String currency, Integer count) {

        log.info("Syncing deposit/withdrawal transactions for currency: {}, count: {}", currency, count);

        // 빗썸 API에서 데이터 조회
        var bithumbResponse = bithumbClient.getDepositWithdrawalStatus(currency, 0, count);

        // DB에 동기화
        syncTransactionsToDatabase(bithumbResponse);

        // DB에서 최신 데이터 조회하여 반환
        return getTransactionSummary(currency);
    }

    /**
     * DB에서 입출금 현황 요약 조회
     */
    public AccountTransactionResponse.AccountTransactionSummaryResponse getTransactionSummary(String currency) {
        List<AccountTransaction> deposits;
        List<AccountTransaction> withdrawals;

        if ("ALL".equalsIgnoreCase(currency)) {
            deposits = accountTransactionRepository.findByTransactionTypeOrderByTransferDateDesc(
                    AccountTransaction.TransactionType.DEPOSIT);
            withdrawals = accountTransactionRepository.findByTransactionTypeOrderByTransferDateDesc(
                    AccountTransaction.TransactionType.WITHDRAWAL);
        } else {
            deposits = accountTransactionRepository.findByTransactionTypeAndOrderCurrencyOrderByTransferDateDesc(
                    AccountTransaction.TransactionType.DEPOSIT, currency);
            withdrawals = accountTransactionRepository.findByTransactionTypeAndOrderCurrencyOrderByTransferDateDesc(
                    AccountTransaction.TransactionType.WITHDRAWAL, currency);
        }

        return AccountTransactionResponse.AccountTransactionSummaryResponse.from(deposits, withdrawals);
    }

    /**
     * 입금 내역만 조회
     */
    public List<AccountTransactionResponse> getDepositTransactions(String currency) {
        List<AccountTransaction> deposits;

        if ("ALL".equalsIgnoreCase(currency)) {
            deposits = accountTransactionRepository.findByTransactionTypeOrderByTransferDateDesc(
                    AccountTransaction.TransactionType.DEPOSIT);
        } else {
            deposits = accountTransactionRepository.findByTransactionTypeAndOrderCurrencyOrderByTransferDateDesc(
                    AccountTransaction.TransactionType.DEPOSIT, currency);
        }

        return AccountTransactionResponse.from(deposits);
    }

    /**
     * 출금 내역만 조회
     */
    public List<AccountTransactionResponse> getWithdrawalTransactions(String currency) {
        List<AccountTransaction> withdrawals;

        if ("ALL".equalsIgnoreCase(currency)) {
            withdrawals = accountTransactionRepository.findByTransactionTypeOrderByTransferDateDesc(
                    AccountTransaction.TransactionType.WITHDRAWAL);
        } else {
            withdrawals = accountTransactionRepository.findByTransactionTypeAndOrderCurrencyOrderByTransferDateDesc(
                    AccountTransaction.TransactionType.WITHDRAWAL, currency);
        }

        return AccountTransactionResponse.from(withdrawals);
    }

    /**
     * 최근 거래 내역 조회
     */
    public List<AccountTransactionResponse> getRecentTransactions() {
        var transactions = accountTransactionRepository.findTop20ByOrderByTransferDateDesc();
        return AccountTransactionResponse.from(transactions);
    }

    /**
     * 기간별 거래 내역 조회
     */
    public List<AccountTransactionResponse> getTransactionsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate) {
        var transactions = accountTransactionRepository.findByTransferDateBetween(startDate, endDate);
        return AccountTransactionResponse.from(transactions);
    }

    /**
     * 빗썸 API 응답을 DB에 동기화
     */
    @Transactional
    private void syncTransactionsToDatabase(BithumbDepositWithdrawalResponse response) {
        if (response.data() == null) {
            log.warn("No data in Bithumb API response");
            return;
        }

        // 입금 내역 동기화
        if (response.data().deposit() != null) {
            response.data().deposit().forEach(this::saveOrUpdateTransaction);
        }

        // 출금 내역 동기화
        if (response.data().withdrawal() != null) {
            response.data().withdrawal().forEach(this::saveOrUpdateTransaction);
        }

        log.info("Successfully synced transactions to database");
    }

    /**
     * 개별 거래 내역 저장 또는 업데이트
     */
    private void saveOrUpdateTransaction(BithumbDepositWithdrawalResponse.Transaction transaction) {
        try {
            var existingTransaction = accountTransactionRepository.findByOrderId(transaction.order_id());

            var transactionType = determineTransactionType(transaction.search());
            var transferDate = parseTransferDate(transaction.transfer_date());
            var units = parseBigDecimal(transaction.units());
            var price = parseBigDecimal(transaction.price());
            var krwAmount = transaction.krw_amount();
            var fee = transaction.fee();
            var orderBalance = parseBigDecimal(transaction.order_balance());

            if (existingTransaction.isPresent()) {
                // 기존 거래 내역 업데이트
                existingTransaction.get().updateTransaction(units, price, krwAmount, fee, orderBalance);
                log.debug("Updated existing transaction: {}", transaction.order_id());
            } else {
                // 새로운 거래 내역 생성
                var newTransaction = new AccountTransaction(
                        transaction.order_id(),
                        transactionType,
                        transaction.order_currency(),
                        transaction.payment_currency(),
                        units,
                        price,
                        krwAmount,
                        fee,
                        orderBalance,
                        transferDate);
                accountTransactionRepository.save(newTransaction);
                log.debug("Created new transaction: {}", transaction.order_id());
            }
        } catch (Exception e) {
            log.error("Failed to save/update transaction: {}, error: {}", transaction.order_id(), e.getMessage());
        }
    }

    /**
     * 거래 유형 결정
     */
    private AccountTransaction.TransactionType determineTransactionType(String search) {
        return switch (search.toLowerCase()) {
            case "deposit", "입금" -> AccountTransaction.TransactionType.DEPOSIT;
            case "withdrawal", "출금" -> AccountTransaction.TransactionType.WITHDRAWAL;
            default -> {
                log.warn("Unknown transaction type: {}, defaulting to DEPOSIT", search);
                yield AccountTransaction.TransactionType.DEPOSIT;
            }
        };
    }

    /**
     * 날짜 문자열 파싱
     */
    private LocalDateTime parseTransferDate(String dateString) {
        try {
            // 빗썸 API 날짜 형식에 맞게 파싱 (예: "2024-01-21 14:30:00")
            var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateString, formatter);
        } catch (Exception e) {
            log.warn("Failed to parse transfer date: {}, using current time", dateString);
            return LocalDateTime.now();
        }
    }

    /**
     * BigDecimal 파싱
     */
    private BigDecimal parseBigDecimal(String value) {
        try {
            return value != null && !value.isEmpty() ? new BigDecimal(value) : BigDecimal.ZERO;
        } catch (NumberFormatException e) {
            log.warn("Failed to parse BigDecimal: {}, using ZERO", value);
            return BigDecimal.ZERO;
        }
    }
}
