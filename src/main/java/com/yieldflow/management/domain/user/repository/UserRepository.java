package com.yieldflow.management.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yieldflow.management.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

}
