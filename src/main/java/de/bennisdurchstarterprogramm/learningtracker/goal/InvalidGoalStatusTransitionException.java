package de.bennisdurchstarterprogramm.learningtracker.goal;

import java.util.UUID;

public class InvalidGoalStatusTransitionException extends RuntimeException {

	private final UUID goalId;
	private final GoalStatus currentStatus;
	private final GoalStatus targetStatus;

	InvalidGoalStatusTransitionException(UUID goalId, GoalStatus currentStatus, GoalStatus targetStatus) {
		super("Cannot move goal " + goalId + " from " + currentStatus + " to " + targetStatus + ".");
		this.goalId = goalId;
		this.currentStatus = currentStatus;
		this.targetStatus = targetStatus;
	}

	public UUID getGoalId() {
		return goalId;
	}

	public GoalStatus getCurrentStatus() {
		return currentStatus;
	}

	public GoalStatus getTargetStatus() {
		return targetStatus;
	}
}
