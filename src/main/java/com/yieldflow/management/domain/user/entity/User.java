package com.yieldflow.management.domain.user.entity;

import java.util.ArrayList;

import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.yieldflow.management.global.enums.UserRole;
import com.yieldflow.management.global.enums.UserStatus;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Email
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus status;

    @Column(nullable = false)
    private boolean isVerified;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<ApiKey> apiKeys = new ArrayList<>();

    @Builder
    public User(String email, String password, String nickname, UserRole role, UserStatus status) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : UserRole.USER;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.isVerified = false;
    }

    public void setVerified() {
        this.isVerified = true;
    }
}
