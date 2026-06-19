package de.bennisdurchstarterprogramm.learningtracker.learninglog;

import java.net.URI;
import java.util.List;

import de.bennisdurchstarterprogramm.learningtracker.goal.Goal;
import de.bennisdurchstarterprogramm.learningtracker.goal.GoalFinder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/learning-logs")
class LearningLogController {

	private final LearningLogRepository learningLogs;
	private final GoalFinder goalFinder;

	LearningLogController(LearningLogRepository learningLogs, GoalFinder goalFinder) {
		this.learningLogs = learningLogs;
		this.goalFinder = goalFinder;
	}

	@PostMapping
	ResponseEntity<LearningLogResponse> createLearningLog(@Valid @RequestBody CreateLearningLogRequest request) {
		Goal goal = request.goalId() == null ? null : goalFinder.requireById(request.goalId());
		LearningLog savedLearningLog = learningLogs.save(
				new LearningLog(request.topic(), request.summary(), request.nextStep(), goal));

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(savedLearningLog.getId())
				.toUri();

		return ResponseEntity.created(location).body(LearningLogResponse.from(savedLearningLog));
	}

	@GetMapping
	List<LearningLogResponse> listLearningLogs() {
		return learningLogs.findAll().stream()
				.map(LearningLogResponse::from)
				.toList();
	}
}
