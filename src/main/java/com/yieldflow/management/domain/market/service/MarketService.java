package com.yieldflow.management.domain.market.service;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yieldflow.management.domain.market.entity.MarketCode;
import com.yieldflow.management.domain.market.repository.MarketCodeRepository;
import com.yieldflow.management.global.external.bithumb.dto.BithumbMarketCodeResponseDto;
import com.yieldflow.management.global.external.bithumb.dto.BithumbVirtualAssetWarningDto;
import com.yieldflow.management.global.external.bithumb.service.BithumbFeignService;

@Service
@RequiredArgsConstructor
@Transactional
public class MarketService {

    private final BithumbFeignService bithumbFeignService;

    private final MarketCodeRepository marketCodeRepository;

    private static final Set<String> TARGET_CURRENCIES = Set.of("BTC", "ETH", "USDT", "USDC");

    public List<BithumbMarketCodeResponseDto> getMarketCodes() {
        List<BithumbMarketCodeResponseDto> apiDataList = bithumbFeignService.getMarketCodes();

        Map<String, MarketCode> existingMarketMap = marketCodeRepository.findAll().stream()
                .collect(Collectors.toMap(MarketCode::getMarket, Function.identity()));

        List<MarketCode> newMarketCodes = new ArrayList<>();

        for (BithumbMarketCodeResponseDto dto : apiDataList) {
            MarketCode marketCode = existingMarketMap.get(dto.market());

            if (marketCode != null) {
                marketCode.updateInfo(dto.koreanName(), dto.englishName(), dto.marketWarning());
            } else {
                MarketCode newMarketCode = MarketCode.builder()
                        .market(dto.market())
                        .koreanName(dto.koreanName())
                        .englishName(dto.englishName())
                        .marketWarning(dto.marketWarning())
                        .build();
                newMarketCodes.add(newMarketCode);
            }
        }

        if (!newMarketCodes.isEmpty()) {
            marketCodeRepository.saveAll(newMarketCodes);
        }
        return bithumbFeignService.getMarketCodes().stream()
                .filter(market -> TARGET_CURRENCIES.stream()
                        .anyMatch(currency -> market.market().contains(currency)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BithumbVirtualAssetWarningDto> getVirtualAssetWarning() {
        return bithumbFeignService.getVirtualAssetWarning();
    }
}
