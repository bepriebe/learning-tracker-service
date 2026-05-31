package de.bennisdurchstarterprogramm.learningtracker.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import de.bennisdurchstarterprogramm.learningtracker.goal.GoalNotFoundException;
import de.bennisdurchstarterprogramm.learningtracker.goal.InvalidGoalStatusTransitionException;

@RestControllerAdvice
class ResourceErrorHandler {

	@ExceptionHandler(GoalNotFoundException.class)
	ProblemDetail handleGoalNotFound(GoalNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND,
				"Goal not found.");
		problem.setTitle("Resource not found");
		problem.setProperty("goalId", exception.getGoalId());

		return problem;
	}

	@ExceptionHandler(InvalidGoalStatusTransitionException.class)
	ProblemDetail handleInvalidGoalStatusTransition(InvalidGoalStatusTransitionException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.CONFLICT,
				"Goal status transition is not allowed.");
		problem.setTitle("Invalid goal status transition");
		problem.setProperty("goalId", exception.getGoalId());
		problem.setProperty("currentStatus", exception.getCurrentStatus());
		problem.setProperty("targetStatus", exception.getTargetStatus());

		return problem;
	}
}
