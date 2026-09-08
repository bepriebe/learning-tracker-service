package io.github.bepriebe.learningtracker.learninglog;

import java.util.UUID;

import io.github.bepriebe.learningtracker.goal.Goal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "goal_id")
	private Goal goal;

    protected LearningLog() {
    }

    LearningLog(String topic, String summary, String nextStep) {
		this(topic, summary, nextStep, null);
	}

	LearningLog(String topic, String summary, String nextStep, Goal goal) {
        this.topic = topic;
        this.summary = summary;
        this.nextStep = nextStep;
		this.goal = goal;
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

	Goal getGoal() {
		return goal;
	}
}
