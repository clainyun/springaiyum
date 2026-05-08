package com.ssafy.yumyum.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MealAnalysis {

    private NutritionSummary nutrition;
    private String headline;
    private String nextAction;
    private String grade;
    private int score;
    private List<String> insights = new ArrayList<>();
}
