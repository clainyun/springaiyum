package com.ssafy.yumyum.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Challenge {

    private String id;
    private String title;
    private String description;
    private String category;
    private int targetCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String createdBy;
    private LocalDateTime createdAt;
}
