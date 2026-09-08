package com.example.demo.service;

import com.example.demo.dto.SpinResultDTO;
import com.example.demo.entity.Prize;
import com.example.demo.entity.SpinHistory;
import com.example.demo.entity.UserSpin;
import com.example.demo.repository.SpinHistoryRepository;
import com.example.demo.repository.UserSpinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpinService {

    private final UserSpinRepository userSpinRepo;
    private final SpinHistoryRepository historyRepo;
    private final ProbabilityService probabilityService;
    private final InventoryService inventoryService;
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_GLOBAL_SPINS = "stats:global_spins";

    @Transactional
    public SpinResultDTO executeSpin(String userId) {
        // 1. Lock DB User (Pessimistic Write Lock - FOR UPDATE)
        UserSpin user = userSpinRepo.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 2. Validate luot
        if (user.getAvailableSpins() <= 0) {
            throw new RuntimeException("Ban da het luot quay!");
        }

        // 3. Chuan bi tham so
        int n = user.getTotalUsed() + 1;

        Long s = redisTemplate.opsForValue().increment(KEY_GLOBAL_SPINS);
        if (s == null) s = 1L;

        // Auto panic factor: S cham 15000 thi bat dau tang, max tai S=20000
        double panicFactor = inventoryService.calculateAutoPanicFactor(s);

        // 4. Tinh WIN/LOSE
        boolean isLucky = probabilityService.calculateResult(n, s, panicFactor);

        int prizeId = 999;
        String prizeName = "Chuc may man lan sau";
        boolean isRealWin = false;

        // 5. Neu Win -> Chon qua tu kho
        if (isLucky) {
            Prize wonPrize = inventoryService.pickPrize();
            if (wonPrize != null) {
                prizeId = wonPrize.getId();
                prizeName = wonPrize.getName();
                isRealWin = true;
            } else {
                log.warn("User {} won probability but inventory empty!", userId);
            }
        }

        // 6. Persistence
        user.setTotalUsed(user.getTotalUsed() + 1);
        user.setLastSpinTime(LocalDateTime.now());
        userSpinRepo.save(user);

        SpinHistory history = new SpinHistory();
        history.setUserId(userId);
        history.setPrizeId(prizeId);
        history.setPrizeName(prizeName);
        history.setWin(isRealWin);
        history.setNIndex(n);
        history.setSIndex(s);
        historyRepo.save(history);

        log.info("SPIN RESULT: userId={}, n={}, S={}, prizeId={}, prizeName={}, isWin={}",
                userId, n, s, prizeId, prizeName, isRealWin);

        return new SpinResultDTO(isRealWin, prizeName, prizeId, user.getAvailableSpins() - 1);
    }

    /**
     * Xem lich su trung thuong cua user
     */
    public List<SpinHistory> getWinHistory(String userId) {
        return historyRepo.findByUserIdAndIsWinTrueOrderByCreatedAtDesc(userId);
    }

    /**
     * Xem toan bo lich su quay cua user
     */
    public List<SpinHistory> getAllHistory(String userId) {
        return historyRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
