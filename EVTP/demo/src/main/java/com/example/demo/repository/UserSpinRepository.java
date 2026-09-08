package com.example.demo.repository;

import com.example.demo.entity.UserSpin;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSpinRepository extends JpaRepository<UserSpin, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserSpin u WHERE u.userId = :userId")
    Optional<UserSpin> findByUserIdForUpdate(String userId);
}
