package com.yieldflow.management.domain.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "social_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_social_provider", columnNames = { "provider", "provider_id" })
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String provider; // Enum으로 관리 가능 (GOOGLE, KAKAO...)

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @Builder
    public SocialAccount(User user, String provider, String providerId) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.connectedAt = LocalDateTime.now();
    }
}
