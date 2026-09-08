package io.github.bepriebe.learningtracker.goal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreateGoalRequest(
		@NotBlank(message = "title must not be blank")
		@Size(max = 200, message = "title must not exceed 200 characters")
		String title,
		@Size(max = 1000, message = "description must not exceed 1000 characters")
		String description) {
}
