package com.yieldflow.management.domain.user.entity;

import java.time.LocalDateTime;

import com.yieldflow.management.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "api_keys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "access_key", length = 64, unique = true)
    private String accessKey;

    @Column(name = "secret_key", length = 255, unique = true)
    private String secretKey;

    @Column(name = "is_active")
    private boolean isActive = true;

    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;

    @Builder
    public ApiKey(User user, String name, String accessKey, String secretKey,
            LocalDateTime expiresAt) {
        this.user = user;
        this.name = name;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.expiresAt = expiresAt;
    }
}
