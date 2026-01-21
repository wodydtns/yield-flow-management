package com.yieldflow.management.domain.account.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 계좌 입출금 거래 내역 엔티티
 */
@Entity
@Table(name = "account_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "order_currency", nullable = false)
    private String orderCurrency;

    @Column(name = "payment_currency", nullable = false)
    private String paymentCurrency;

    @Column(name = "units", precision = 20, scale = 8)
    private BigDecimal units;

    @Column(name = "price", precision = 20, scale = 8)
    private BigDecimal price;

    @Column(name = "krw_amount", precision = 20, scale = 2)
    private BigDecimal krwAmount;

    @Column(name = "fee", precision = 20, scale = 8)
    private BigDecimal fee;

    @Column(name = "order_balance", precision = 20, scale = 8)
    private BigDecimal orderBalance;

    @Column(name = "transfer_date")
    private LocalDateTime transferDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TransactionType {
        DEPOSIT("입금"),
        WITHDRAWAL("출금");

        private final String description;

        TransactionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public AccountTransaction(String orderId, TransactionType transactionType, String orderCurrency,
            String paymentCurrency, BigDecimal units, BigDecimal price,
            BigDecimal krwAmount, BigDecimal fee, BigDecimal orderBalance,
            LocalDateTime transferDate) {
        this.orderId = orderId;
        this.transactionType = transactionType;
        this.orderCurrency = orderCurrency;
        this.paymentCurrency = paymentCurrency;
        this.units = units;
        this.price = price;
        this.krwAmount = krwAmount;
        this.fee = fee;
        this.orderBalance = orderBalance;
        this.transferDate = transferDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTransaction(BigDecimal units, BigDecimal price, BigDecimal krwAmount,
            BigDecimal fee, BigDecimal orderBalance) {
        this.units = units;
        this.price = price;
        this.krwAmount = krwAmount;
        this.fee = fee;
        this.orderBalance = orderBalance;
        this.updatedAt = LocalDateTime.now();
    }
}
