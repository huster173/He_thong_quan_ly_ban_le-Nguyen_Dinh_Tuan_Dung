package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spin_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpinHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "prize_id", nullable = false)
    private int prizeId;

    @Column(name = "prize_name", length = 100)
    private String prizeName;

    @Column(name = "is_win")
    private boolean isWin;

    @Column(name = "n_index")
    private int nIndex;

    @Column(name = "s_index")
    private long sIndex;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
