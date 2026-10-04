package com.examforge.util;

import java.time.LocalDateTime;
import java.util.List;

public class ExposureScorer {
    private static final double RECENT_EXAM_1_PENALTY = 100.0;
    private static final double RECENT_EXAM_2_PENALTY = 70.0;
    private static final double RECENT_EXAM_3_PENALTY = 40.0;
    private static final double OLDER_EXAM_PENALTY = 10.0;
    private static final double NEVER_USED_SCORE = 0.0;

    private static ExposureScorer instance;

    private ExposureScorer() {}

    public static synchronized ExposureScorer getInstance() {
        if (instance == null) {
            instance = new ExposureScorer();
        }
        return instance;
    }

    /**
     * Calculate exposure penalty for a question based on its usage history.
     * Lower penalty is better (0 = never used = best).
     * 
     * Formula:
     * - Last finalized exam: 100 penalty units
     * - 2 exams ago: 70 penalty units
     * - 3 exams ago: 40 penalty units
     * - Older than recent window: 10 penalty units
     * - Never used: 0 penalty units
     * 
     * Additional frequency penalty: each additional usage beyond 1 adds 5 units.
     *
     * @param recencyPositions ordered list of positions (0 = most recent)
     * @param totalUsage total official usage count
     * @return penalty score (higher = more exposed = worse)
     */
    public double calculatePenalty(List<Integer> recencyPositions, int totalUsage) {
        if (recencyPositions == null || recencyPositions.isEmpty()) {
            return NEVER_USED_SCORE;
        }

        double penalty = 0;
        for (int pos : recencyPositions) {
            switch (pos) {
                case 0: penalty += RECENT_EXAM_1_PENALTY; break;
                case 1: penalty += RECENT_EXAM_2_PENALTY; break;
                case 2: penalty += RECENT_EXAM_3_PENALTY; break;
                default: penalty += OLDER_EXAM_PENALTY; break;
            }
        }

        if (totalUsage > 1) {
            penalty += (totalUsage - 1) * 5.0;
        }

        return penalty;
    }

    /**
     * Convert penalty to a preference score (0 to 1).
     * Higher is better (less exposed).
     */
    public double calculatePreferenceScore(double penalty, double maxPenalty) {
        if (maxPenalty <= 0) return 1.0;
        return Math.max(0, 1.0 - (penalty / maxPenalty));
    }

    /**
     * Check if question should be blocked based on recency.
     * @param recencyPosition 0 = most recent, -1 = not in recent window
     * @param recentWindowSize number of recent exams to consider
     * @return true if question should be blocked
     */
    public boolean shouldBlock(int recencyPosition, int recentWindowSize) {
        return recencyPosition >= 0 && recencyPosition < recentWindowSize;
    }
}
