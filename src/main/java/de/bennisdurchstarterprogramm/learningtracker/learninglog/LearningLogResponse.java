package de.bennisdurchstarterprogramm.learningtracker.learninglog;

import java.util.UUID;

record LearningLogResponse(UUID id, String topic, String summary, String nextStep) {

    static LearningLogResponse from(LearningLog learningLog) {
        return new LearningLogResponse(
                learningLog.getId(),
                learningLog.getTopic(),
                learningLog.getSummary(),
                learningLog.getNextStep());
    }
}
