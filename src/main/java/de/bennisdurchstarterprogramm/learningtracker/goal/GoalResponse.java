package de.bennisdurchstarterprogramm.learningtracker.goal;

import java.util.UUID;

record GoalResponse(UUID id, String title, String description, GoalStatus status) {

	static GoalResponse from(Goal goal) {
		return new GoalResponse(goal.getId(), goal.getTitle(), goal.getDescription(), goal.getStatus());
	}
}
