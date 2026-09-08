package com.example.demo.service;

import com.example.demo.entity.Prize;
import com.example.demo.repository.PrizeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final StringRedisTemplate redisTemplate;
    private final PrizeRepository prizeRepository;

    // Cache danh sach qua REAL - load 1 lan khi khoi dong, tranh query DB moi lan quay
    private final CopyOnWriteArrayList<Prize> cachedRealPrizes = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void loadPrizeCache() {
        cachedRealPrizes.clear();
        cachedRealPrizes.addAll(prizeRepository.findByType("REAL"));
        log.info("Loaded {} REAL prizes into cache", cachedRealPrizes.size());
    }

    /**
     * Random chon 1 mon qua REAL con hang va tru kho (Atomic Decrement)
     * @return Prize entity neu con hang, null neu het sach
     */
    public Prize pickPrize() {
        String today = LocalDate.now().toString();

        // Dung cached list thay vi query DB moi lan
        List<Prize> candidates = new ArrayList<>(cachedRealPrizes);
        Collections.shuffle(candidates, ThreadLocalRandom.current());

        for (Prize prize : candidates) {
            String key = "inventory:" + today + ":" + prize.getId();

            String val = redisTemplate.opsForValue().get(key);
            if (val != null && Integer.parseInt(val) > 0) {
                Long remaining = redisTemplate.opsForValue().decrement(key);

                if (remaining != null && remaining >= 0) {
                    log.info("PRIZE PICKED: prizeId={}, name={}, remaining={}", prize.getId(), prize.getName(), remaining);
                    return prize;
                }
            }
        }

        log.warn("INVENTORY EMPTY: All prizes out of stock for {}", today);
        return null;
    }

    // Nguong bat dau tang panic (S_CAP trong cong thuc)
    private static final long PANIC_START = 15000;
    // Gioi han max he thong
    private static final long S_MAX = 20000;
    // Khoang tu 15000 -> 20000 de xa qua
    private static final double PANIC_WINDOW = S_MAX - PANIC_START; // 5000

    /**
     * Tu dong tinh panic factor dua tren S (tong luot quay he thong).
     *
     * S < 15000:  factor = 1.0 (binh thuong, W_sys dang tang dan)
     * S = 15000 -> 20000: factor tang dan tu 1.0 -> 100.0 (xa qua)
     * S >= 20000: factor = 100.0 (max)
     *
     * Cong thuc: factor = 1 + 99 * ((S - 15000) / 5000)^2
     * Dung luy thua 2 de tang cham dau, tang nhanh cuoi.
     *
     * @param s Tong luot quay S cua toan he thong
     */
    public double calculateAutoPanicFactor(long s) {
        // Chua den nguong -> binh thuong
        if (s < PANIC_START) {
            return 1.0;
        }

        // Vuot max -> panic toi da
        if (s >= S_MAX) {
            return 100.0;
        }

        // Tinh ti le tien trinh trong vung panic (0.0 -> 1.0)
        double progress = (s - PANIC_START) / PANIC_WINDOW;

        // Luy thua 2: tang cham o 15000-17500, tang manh o 17500-20000
        double factor = 1.0 + 99.0 * (progress * progress);

        log.debug("AUTO PANIC: S={}, progress={}, factor={}",
                s, String.format("%.3f", progress), String.format("%.1f", factor));

        return factor;
    }
}
