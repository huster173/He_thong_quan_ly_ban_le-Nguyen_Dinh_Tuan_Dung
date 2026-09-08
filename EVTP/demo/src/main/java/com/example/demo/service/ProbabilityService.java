package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProbabilityService {

    private static final double P_BASE = 0.0001;     // 0.01%
    private static final double P_STEP = 0.00181;     // Buoc nhay ~0.181%
    private static final double S_CAP = 15000.0;      // Nguong bao hoa he thong
    private static final double W_MIN = 1.0;          // He so nhan min
    private static final double W_MAX = 50.0;         // He so nhan max
    private static final double W_SLOPE = (W_MAX - W_MIN) / S_CAP; // 49/15000

    /**
     * Tinh toan Win/Lose dua tren cong thuc xac suat dong
     * P_user(n) = 0.0001 + (n-1) * 0.00181
     * W_sys(S)  = 1 + S * (49/15000), max = 50
     * P_final   = P_user * W_sys * panicFactor, cap tai 1.0
     *
     * @param n           Luot quay thu n cua ca nhan (>= 1)
     * @param s           Tong luot quay S cua toan he thong
     * @param panicFactor He so nhan Panic Mode (mac dinh = 1.0)
     * @return true neu trung, false neu truot
     */
    public boolean calculateResult(int n, long s, double panicFactor) {
        // Buoc 1: Tinh P_user(n)
        int effectiveN = Math.max(1, n);
        double pUser = P_BASE + ((effectiveN - 1) * P_STEP);

        // Buoc 2: Tinh W_sys(S)
        double wSys;
        if (s >= S_CAP) {
            wSys = W_MAX;
        } else {
            wSys = W_MIN + (s * W_SLOPE);
        }

        // Buoc 3: Tinh P_final
        double pFinal = pUser * wSys * panicFactor;
        pFinal = Math.min(1.0, pFinal);

        // Buoc 4: Quay so (ThreadLocalRandom - thread-safe cho 30k concurrent users)
        double randomVal = java.util.concurrent.ThreadLocalRandom.current().nextDouble();

        log.info("SPIN CALC: n={}, S={}, panicFactor={} => P_user={}, W_sys={}, P_final={} | Roll={} => {}",
                n, s, panicFactor,
                String.format("%.5f", pUser),
                String.format("%.2f", wSys),
                String.format("%.5f", pFinal),
                String.format("%.5f", randomVal),
                (randomVal < pFinal ? "WIN" : "LOSE"));

        return randomVal < pFinal;
    }
}
