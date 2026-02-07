package com.yieldflow.management.domain.market.entity;

import java.time.LocalDateTime;

import com.yieldflow.management.global.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "market_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MarketCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "market", nullable = false, length = 20)
    private String market;

    @Column(name = "korean_name", nullable = false, length = 20)
    private String koreanName;

    @Column(name = "english_name", nullable = false, length = 20)
    private String englishName;

    @Column(name = "market_warning", nullable = false, length = 20)
    private String marketWarning;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 마켓 정보 업데이트
     */
    public void updateInfo(String koreanName, String englishName, String marketWarning) {
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.marketWarning = marketWarning;
    }
}
