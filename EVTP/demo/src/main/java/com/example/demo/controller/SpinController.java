package com.example.demo.controller;

import com.example.demo.dto.SpinResultDTO;
import com.example.demo.entity.SpinHistory;
import com.example.demo.service.SpinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SpinController {

    private final SpinService spinService;
    private final StringRedisTemplate redisTemplate;

    @PostMapping("/spin")
    public ResponseEntity<?> spin(@RequestParam String userId) {
        // Rate Limit: 1 User chi duoc goi 1 lan moi 3 giay
        String rateLimitKey = "ratelimit:" + userId;
        Boolean isAllowed = redisTemplate.opsForValue()
                .setIfAbsent(rateLimitKey, "1", 3, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(isAllowed)) {
            return ResponseEntity.status(429)
                    .body("Ban thao tac qua nhanh! Vui long doi 3 giay.");
        }

        try {
            SpinResultDTO result = spinService.executeSpin(userId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/wins")
    public ResponseEntity<List<SpinHistory>> getWinHistory(@RequestParam String userId) {
        return ResponseEntity.ok(spinService.getWinHistory(userId));
    }

    @GetMapping("/history/all")
    public ResponseEntity<List<SpinHistory>> getAllHistory(@RequestParam String userId) {
        return ResponseEntity.ok(spinService.getAllHistory(userId));
    }
}
