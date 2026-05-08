package com.ssafy.yumyum.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CoachAdvice {

    private String summary;
    private String recovery;
    private List<String> nextActions = new ArrayList<>();
    private List<WorkoutSession> sessions = new ArrayList<>();
    private List<Meal> recentMeals = new ArrayList<>();
    private List<MealAnalysis> recentAnalyses = new ArrayList<>();
}
