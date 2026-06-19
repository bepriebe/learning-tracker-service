package de.bennisdurchstarterprogramm.learningtracker.learninglog;

import java.util.UUID;

record LearningLogResponse(UUID id, String topic, String summary, String nextStep, UUID goalId) {

    static LearningLogResponse from(LearningLog learningLog) {
        return new LearningLogResponse(
                learningLog.getId(),
                learningLog.getTopic(),
                learningLog.getSummary(),
				learningLog.getNextStep(),
				learningLog.getGoal() == null ? null : learningLog.getGoal().getId());
    }
}
