package com.examforge.config;

public class SearchConfig {
    private int maxCombinations = 100000;
    private int maxTimePerAttemptMs = 30000;
    private boolean enableEarlyPruning = true;

    private static SearchConfig instance;

    public static synchronized SearchConfig getInstance() {
        if (instance == null) {
            instance = new SearchConfig();
        }
        return instance;
    }

    public int getMaxCombinations() { return maxCombinations; }
    public void setMaxCombinations(int max) { this.maxCombinations = max; }
    public int getMaxTimePerAttemptMs() { return maxTimePerAttemptMs; }
    public void setMaxTimePerAttemptMs(int ms) { this.maxTimePerAttemptMs = ms; }
    public boolean isEnableEarlyPruning() { return enableEarlyPruning; }
    public void setEnableEarlyPruning(boolean b) { this.enableEarlyPruning = b; }
}
