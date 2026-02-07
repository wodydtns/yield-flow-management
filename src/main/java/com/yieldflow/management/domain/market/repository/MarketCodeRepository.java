package com.yieldflow.management.domain.market.repository;

import com.yieldflow.management.domain.market.entity.MarketCode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 마켓 코드 리포지토리
 */
@Repository
public interface MarketCodeRepository extends JpaRepository<MarketCode, Long> {

    Optional<MarketCode> findByMarket(String market);

    boolean existsByMarket(String market);
}
