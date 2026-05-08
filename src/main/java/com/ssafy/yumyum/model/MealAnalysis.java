package com.ssafy.yumyum.model;

import java.util.ArrayList;
import java.util.List;

public class MealAnalysis {

    private NutritionSummary nutrition;
    private String headline;
    private String nextAction;
    private String grade;
    private int score;
    private List<String> insights = new ArrayList<>();

    public NutritionSummary getNutrition() {
        return nutrition;
    }

    public void setNutrition(NutritionSummary nutrition) {
        this.nutrition = nutrition;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getInsights() {
        return insights;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }
}
