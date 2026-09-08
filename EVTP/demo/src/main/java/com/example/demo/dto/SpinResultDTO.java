package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpinResultDTO {

    private boolean win;
    private String prizeName;
    private int prizeId;
    private int remainingSpins;
}
