package com.example.demo.repository;

import com.example.demo.entity.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Integer> {

    List<Prize> findByType(String type);
}
