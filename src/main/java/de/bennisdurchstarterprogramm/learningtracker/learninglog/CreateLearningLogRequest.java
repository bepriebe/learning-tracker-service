package de.bennisdurchstarterprogramm.learningtracker.learninglog;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreateLearningLogRequest(
        @NotBlank(message = "topic must not be blank")
        @Size(max = 200, message = "topic must not exceed 200 characters")
        String topic,

        @NotBlank(message = "summary must not be blank")
        @Size(max = 2000, message = "summary must not exceed 2000 characters")
        String summary,

        @Size(max = 1000, message = "nextStep must not exceed 1000 characters")
        String nextStep,

		UUID goalId) {
}
