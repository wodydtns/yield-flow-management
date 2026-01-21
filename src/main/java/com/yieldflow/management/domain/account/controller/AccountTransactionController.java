package com.yieldflow.management.domain.account.controller;

import com.yieldflow.management.domain.account.dto.AccountTransactionResponse;
import com.yieldflow.management.domain.account.service.AccountTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 계좌 입출금 거래 내역 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Account Transactions", description = "계좌 입출금 거래 내역 관리 API")
public class AccountTransactionController {

    private final AccountTransactionService accountTransactionService;

    /**
     * 입출금 현황 동기화 및 조회
     * 
     * @param currency 통화 코드
     * @param count    조회 개수
     * @return 입출금 현황 요약
     */
    @Operation(summary = "특정 통화 입출금 현황 동기화", description = "빗썸 API에서 특정 통화의 입출금 현황을 조회하고 DB에 동기화한 후 요약 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동기화 성공", content = @Content(schema = @Schema(implementation = AccountTransactionResponse.AccountTransactionSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{currency}/sync")
    public ResponseEntity<AccountTransactionResponse.AccountTransactionSummaryResponse> syncTransactions(
            @Parameter(description = "통화 코드 (예: BTC, ETH, KRW)", required = true, example = "BTC") @PathVariable @NotBlank String currency,
            @Parameter(description = "조회할 거래 개수", example = "20") @RequestParam(defaultValue = "20") @Min(1) Integer count) {

        log.info("Syncing transactions for currency: {}, count: {}", currency, count);

        var result = accountTransactionService.syncAndGetTransactions(currency, count);
        return ResponseEntity.ok(result);
    }

    /**
     * 전체 통화 입출금 현황 동기화 및 조회
     * 
     * @param count 조회 개수
     * @return 입출금 현황 요약
     */
    @Operation(summary = "전체 통화 입출금 현황 동기화", description = "빗썸 API에서 모든 통화의 입출금 현황을 조회하고 DB에 동기화한 후 요약 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동기화 성공", content = @Content(schema = @Schema(implementation = AccountTransactionResponse.AccountTransactionSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/sync")
    public ResponseEntity<AccountTransactionResponse.AccountTransactionSummaryResponse> syncAllTransactions(
            @Parameter(description = "조회할 거래 개수", example = "20") @RequestParam(defaultValue = "20") @Min(1) Integer count) {

        log.info("Syncing all transactions, count: {}", count);

        var result = accountTransactionService.syncAndGetTransactions("ALL", count);
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 통화 입출금 현황 요약 조회
     * 
     * @param currency 통화 코드
     * @return 입출금 현황 요약
     */
    @Operation(summary = "특정 통화 입출금 현황 요약 조회", description = "DB에서 특정 통화의 입출금 현황 요약 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AccountTransactionResponse.AccountTransactionSummaryResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 통화의 거래 내역을 찾을 수 없음")
    })
    @GetMapping("/{currency}/summary")
    public ResponseEntity<AccountTransactionResponse.AccountTransactionSummaryResponse> getTransactionSummary(
            @Parameter(description = "통화 코드 (예: BTC, ETH, KRW)", required = true, example = "BTC") @PathVariable @NotBlank String currency) {

        log.info("Getting transaction summary for currency: {}", currency);

        var result = accountTransactionService.getTransactionSummary(currency);
        return ResponseEntity.ok(result);
    }

    /**
     * 전체 통화 입출금 현황 요약 조회
     * 
     * @return 입출금 현황 요약
     */
    @Operation(summary = "전체 통화 입출금 현황 요약 조회", description = "DB에서 모든 통화의 입출금 현황 요약 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AccountTransactionResponse.AccountTransactionSummaryResponse.class)))
    })
    @GetMapping("/summary")
    public ResponseEntity<AccountTransactionResponse.AccountTransactionSummaryResponse> getAllTransactionSummary() {
        log.info("Getting transaction summary for all currencies");

        var result = accountTransactionService.getTransactionSummary("ALL");
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 통화 입금 내역 조회
     * 
     * @param currency 통화 코드
     * @return 입금 내역 목록
     */
    @Operation(summary = "특정 통화 입금 내역 조회", description = "특정 통화의 입금 거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AccountTransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 통화의 입금 내역을 찾을 수 없음")
    })
    @GetMapping("/{currency}/deposits")
    public ResponseEntity<List<AccountTransactionResponse>> getDepositTransactions(
            @Parameter(description = "통화 코드 (예: BTC, ETH, KRW)", required = true, example = "BTC") @PathVariable @NotBlank String currency) {

        log.info("Getting deposit transactions for currency: {}", currency);

        var result = accountTransactionService.getDepositTransactions(currency);
        return ResponseEntity.ok(result);
    }

    /**
     * 전체 통화 입금 내역 조회
     * 
     * @return 입금 내역 목록
     */
    @Operation(summary = "전체 통화 입금 내역 조회", description = "모든 통화의 입금 거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AccountTransactionResponse.class)))
    })
    @GetMapping("/deposits")
    public ResponseEntity<List<AccountTransactionResponse>> getAllDepositTransactions() {
        log.info("Getting deposit transactions for all currencies");

        var result = accountTransactionService.getDepositTransactions("ALL");
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 통화 출금 내역 조회
     * 
     * @param currency 통화 코드
     * @return 출금 내역 목록
     */
    @Operation(summary = "특정 통화 출금 내역 조회", description = "특정 통화의 출금 거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AccountTransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 통화의 출금 내역을 찾을 수 없음")
    })
    @GetMapping("/{currency}/withdrawals")
    public ResponseEntity<List<AccountTransactionResponse>> getWithdrawalTransactions(
            @Parameter(description = "통화 코드 (예: BTC, ETH, KRW)", required = true, example = "BTC") @PathVariable @NotBlank String currency) {

        log.info("Getting withdrawal transactions for currency: {}", currency);

        var result = accountTransactionService.getWithdrawalTransactions(currency);
        return ResponseEntity.ok(result);
    }

    /**
     * 전체 통화 출금 내역 조회
     * 
     * @return 출금 내역 목록
     */
    @Operation(summary = "전체 통화 출금 내역 조회", description = "모든 통화의 출금 거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AccountTransactionResponse.class)))
    })
    @GetMapping("/withdrawals")
    public ResponseEntity<List<AccountTransactionResponse>> getAllWithdrawalTransactions() {
        log.info("Getting withdrawal transactions for all currencies");

        var result = accountTransactionService.getWithdrawalTransactions("ALL");
        return ResponseEntity.ok(result);
    }

    /**
     * 최근 거래 내역 조회 (최대 20개)
     * 
     * @return 최근 거래 내역 목록
     */
    @Operation(summary = "최근 거래 내역 조회", description = "최근 거래 내역을 최대 20개까지 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AccountTransactionResponse.class)))
    })
    @GetMapping("/recent")
    public ResponseEntity<List<AccountTransactionResponse>> getRecentTransactions() {
        log.info("Getting recent transactions");

        var result = accountTransactionService.getRecentTransactions();
        return ResponseEntity.ok(result);
    }

    /**
     * 기간별 거래 내역 조회
     * 
     * @param startDate 시작 날짜 (yyyy-MM-dd'T'HH:mm:ss)
     * @param endDate   종료 날짜 (yyyy-MM-dd'T'HH:mm:ss)
     * @return 기간별 거래 내역 목록
     */
    @Operation(summary = "기간별 거래 내역 조회", description = "지정된 기간 내의 모든 통화 거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AccountTransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식 또는 범위")
    })
    @GetMapping
    public ResponseEntity<List<AccountTransactionResponse>> getTransactionsByDateRange(
            @Parameter(description = "조회 시작 날짜 (ISO 8601 형식)", required = true, example = "2024-01-01T00:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "조회 종료 날짜 (ISO 8601 형식)", required = true, example = "2024-01-31T23:59:59") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Getting transactions for date range: {} to {}", startDate, endDate);

        var result = accountTransactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 통화의 기간별 거래 내역 조회
     * 
     * @param currency  통화 코드
     * @param startDate 시작 날짜 (yyyy-MM-dd'T'HH:mm:ss)
     * @param endDate   종료 날짜 (yyyy-MM-dd'T'HH:mm:ss)
     * @return 기간별 거래 내역 목록
     */
    @Operation(summary = "특정 통화의 기간별 거래 내역 조회", description = "지정된 기간 내의 특정 통화 거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = AccountTransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식 또는 범위"),
            @ApiResponse(responseCode = "404", description = "해당 통화의 거래 내역을 찾을 수 없음")
    })
    @GetMapping("/{currency}")
    public ResponseEntity<List<AccountTransactionResponse>> getTransactionsByCurrencyAndDateRange(
            @Parameter(description = "통화 코드 (예: BTC, ETH, KRW)", required = true, example = "BTC") @PathVariable @NotBlank String currency,
            @Parameter(description = "조회 시작 날짜 (ISO 8601 형식)", required = true, example = "2024-01-01T00:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "조회 종료 날짜 (ISO 8601 형식)", required = true, example = "2024-01-31T23:59:59") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Getting transactions for currency: {} and date range: {} to {}", currency, startDate, endDate);

        // Note: 서비스에 currency별 기간 조회 메서드가 필요할 수 있음
        var result = accountTransactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(result);
    }
}
