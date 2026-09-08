package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_spins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSpin {

    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "total_earned")
    private int totalEarned;

    @Column(name = "total_used")
    private int totalUsed;

    @Column(name = "last_spin_time")
    private LocalDateTime lastSpinTime;

    public int getAvailableSpins() {
        return totalEarned - totalUsed;
    }
}
