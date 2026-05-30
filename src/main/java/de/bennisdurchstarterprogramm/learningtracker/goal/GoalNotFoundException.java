package de.bennisdurchstarterprogramm.learningtracker.goal;

import java.util.UUID;

public class GoalNotFoundException extends RuntimeException {

	private final UUID goalId;

	GoalNotFoundException(UUID goalId) {
		super("Goal not found: " + goalId);
		this.goalId = goalId;
	}

	public UUID getGoalId() {
		return goalId;
	}
}
