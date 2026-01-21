package com.yieldflow.management.domain.account.repository;

import com.yieldflow.management.domain.account.entity.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 계좌 입출금 거래 내역 리포지토리
 */
@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    /**
     * 주문 ID로 거래 내역 조회
     */
    Optional<AccountTransaction> findByOrderId(String orderId);

    /**
     * 거래 유형별 거래 내역 조회
     */
    List<AccountTransaction> findByTransactionTypeOrderByTransferDateDesc(
            AccountTransaction.TransactionType transactionType);

    /**
     * 통화별 거래 내역 조회
     */
    List<AccountTransaction> findByOrderCurrencyOrderByTransferDateDesc(String orderCurrency);

    /**
     * 거래 유형과 통화별 거래 내역 조회
     */
    List<AccountTransaction> findByTransactionTypeAndOrderCurrencyOrderByTransferDateDesc(
            AccountTransaction.TransactionType transactionType, String orderCurrency);

    /**
     * 기간별 거래 내역 조회
     */
    @Query("SELECT at FROM AccountTransaction at WHERE at.transferDate BETWEEN :startDate AND :endDate ORDER BY at.transferDate DESC")
    List<AccountTransaction> findByTransferDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 거래 유형과 기간별 거래 내역 조회
     */
    @Query("SELECT at FROM AccountTransaction at WHERE at.transactionType = :transactionType AND at.transferDate BETWEEN :startDate AND :endDate ORDER BY at.transferDate DESC")
    List<AccountTransaction> findByTransactionTypeAndTransferDateBetween(
            @Param("transactionType") AccountTransaction.TransactionType transactionType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 최근 거래 내역 조회 (제한된 개수)
     */
    List<AccountTransaction> findTop20ByOrderByTransferDateDesc();

    /**
     * 거래 유형별 최근 거래 내역 조회 (제한된 개수)
     */
    List<AccountTransaction> findTop20ByTransactionTypeOrderByTransferDateDesc(
            AccountTransaction.TransactionType transactionType);
}
