package com.ssafy.yumyum.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Meal {

    private String id;
    private String userId;
    private LocalDate mealDate;
    private String mealType;
    private String memo;
    private List<FoodItem> foods = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
