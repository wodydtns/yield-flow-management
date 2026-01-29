package com.yieldflow.management.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yieldflow.management.domain.user.entity.User;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {

    User findByEmailAndPassword(String email, String password);
}
