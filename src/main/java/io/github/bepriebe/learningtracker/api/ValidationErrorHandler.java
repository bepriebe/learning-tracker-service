package io.github.bepriebe.learningtracker.api;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ValidationErrorHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleInvalidRequest(MethodArgumentNotValidException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Request validation failed.");
		problem.setTitle("Invalid request");

		Map<String, String> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(
						fieldError -> fieldError.getField(),
						fieldError -> fieldError.getDefaultMessage() == null
								? "invalid value"
								: fieldError.getDefaultMessage(),
						(firstMessage, ignoredMessage) -> firstMessage));
		problem.setProperty("errors", fieldErrors);

		return problem;
	}
}
