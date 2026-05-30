package de.bennisdurchstarterprogramm.learningtracker.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import de.bennisdurchstarterprogramm.learningtracker.goal.GoalNotFoundException;

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
}
