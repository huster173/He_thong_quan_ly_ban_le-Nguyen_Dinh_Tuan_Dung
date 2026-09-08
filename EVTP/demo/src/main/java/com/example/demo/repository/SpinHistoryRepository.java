package com.example.demo.repository;

import com.example.demo.entity.SpinHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {

    List<SpinHistory> findByUserIdOrderByCreatedAtDesc(String userId);

    List<SpinHistory> findByUserIdAndIsWinTrueOrderByCreatedAtDesc(String userId);
}
