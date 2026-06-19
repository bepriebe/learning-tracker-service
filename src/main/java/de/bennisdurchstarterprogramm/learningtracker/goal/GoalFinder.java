package de.bennisdurchstarterprogramm.learningtracker.goal;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class GoalFinder {

	private final GoalRepository goals;

	GoalFinder(GoalRepository goals) {
		this.goals = goals;
	}

	public Goal requireById(UUID id) {
		return goals.findById(id)
				.orElseThrow(() -> new GoalNotFoundException(id));
	}
}
