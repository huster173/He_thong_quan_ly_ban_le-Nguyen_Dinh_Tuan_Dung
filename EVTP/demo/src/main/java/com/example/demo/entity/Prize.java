package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prizes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String type; // 'REAL' (hien vat), 'LUCKY' (loi chuc)

    @Column(name = "daily_quota")
    private int dailyQuota;

    @Column(name = "image_url")
    private String imageUrl;
}
