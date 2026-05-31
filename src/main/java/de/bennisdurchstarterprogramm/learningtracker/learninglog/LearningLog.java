package de.bennisdurchstarterprogramm.learningtracker.learninglog;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_logs")
class LearningLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 200)
    private String topic;

    @Column(nullable = false, length = 2000)
    private String summary;

    @Column(length = 1000)
    private String nextStep;

    protected LearningLog() {
    }

    LearningLog(String topic, String summary, String nextStep) {
        this.topic = topic;
        this.summary = summary;
        this.nextStep = nextStep;
    }

    UUID getId() {
        return id;
    }

    String getTopic() {
        return topic;
    }

    String getSummary() {
        return summary;
    }

    String getNextStep() {
        return nextStep;
    }
}
