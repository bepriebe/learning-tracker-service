package de.bennisdurchstarterprogramm.learningtracker.goal;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "goals")
class Goal {

	@Id
	@GeneratedValue
	private UUID id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(length = 1000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private GoalStatus status = GoalStatus.TODO;

	protected Goal() {
	}

	Goal(String title, String description) {
		this.title = title;
		this.description = description;
	}

	void update(String title, String description) {
		this.title = title;
		this.description = description;
	}

	void start() {
		this.status = GoalStatus.IN_PROGRESS;
	}

	void complete() {
		if (status != GoalStatus.IN_PROGRESS) {
			throw new InvalidGoalStatusTransitionException(id, status, GoalStatus.DONE);
		}

		this.status = GoalStatus.DONE;
	}

	UUID getId() {
		return id;
	}

	String getTitle() {
		return title;
	}

	String getDescription() {
		return description;
	}

	GoalStatus getStatus() {
		return status;
	}
}
