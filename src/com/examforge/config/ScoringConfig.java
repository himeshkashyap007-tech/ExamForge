package com.examforge.config;

public class ScoringConfig {
    private double constraintFitWeight = 0.35;
    private double exposurePreferenceWeight = 0.25;
    private double unitFitWeight = 0.15;
    private double difficultyFitWeight = 0.15;
    private double typeFitWeight = 0.10;

    private static ScoringConfig instance;

    public static synchronized ScoringConfig getInstance() {
        if (instance == null) {
            instance = new ScoringConfig();
        }
        return instance;
    }

    public double getConstraintFitWeight() { return constraintFitWeight; }
    public void setConstraintFitWeight(double w) { this.constraintFitWeight = w; }
    public double getExposurePreferenceWeight() { return exposurePreferenceWeight; }
    public void setExposurePreferenceWeight(double w) { this.exposurePreferenceWeight = w; }
    public double getUnitFitWeight() { return unitFitWeight; }
    public void setUnitFitWeight(double w) { this.unitFitWeight = w; }
    public double getDifficultyFitWeight() { return difficultyFitWeight; }
    public void setDifficultyFitWeight(double w) { this.difficultyFitWeight = w; }
    public double getTypeFitWeight() { return typeFitWeight; }
    public void setTypeFitWeight(double w) { this.typeFitWeight = w; }

    public double getTotalWeight() {
        return constraintFitWeight + exposurePreferenceWeight + unitFitWeight + difficultyFitWeight + typeFitWeight;
    }
}
