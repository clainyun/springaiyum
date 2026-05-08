package com.ssafy.yumyum.model;

import java.util.ArrayList;
import java.util.List;

public class CoachAdvice {

    private String summary;
    private String recovery;
    private List<String> nextActions = new ArrayList<>();
    private List<WorkoutSession> sessions = new ArrayList<>();
    private List<Meal> recentMeals = new ArrayList<>();
    private List<MealAnalysis> recentAnalyses = new ArrayList<>();

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRecovery() {
        return recovery;
    }

    public void setRecovery(String recovery) {
        this.recovery = recovery;
    }

    public List<String> getNextActions() {
        return nextActions;
    }

    public void setNextActions(List<String> nextActions) {
        this.nextActions = nextActions;
    }

    public List<WorkoutSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<WorkoutSession> sessions) {
        this.sessions = sessions;
    }

    public List<Meal> getRecentMeals() {
        return recentMeals;
    }

    public void setRecentMeals(List<Meal> recentMeals) {
        this.recentMeals = recentMeals;
    }

    public List<MealAnalysis> getRecentAnalyses() {
        return recentAnalyses;
    }

    public void setRecentAnalyses(List<MealAnalysis> recentAnalyses) {
        this.recentAnalyses = recentAnalyses;
    }
}
