package de.bennisdurchstarterprogramm.learningtracker.goal;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/goals")
class GoalController {

	private final GoalRepository goals;

	GoalController(GoalRepository goals) {
		this.goals = goals;
	}

	@GetMapping
	List<GoalResponse> listGoals() {
		return goals.findAll().stream()
				.map(GoalResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	GoalResponse getGoal(@PathVariable UUID id) {
		return goals.findById(id)
				.map(GoalResponse::from)
				.orElseThrow(() -> new GoalNotFoundException(id));
	}

	@PostMapping
	ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody CreateGoalRequest request) {
		Goal savedGoal = goals.save(new Goal(request.title(), request.description()));
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(savedGoal.getId())
				.toUri();

		return ResponseEntity.created(location).body(GoalResponse.from(savedGoal));
	}

	@PutMapping("/{id}")
	GoalResponse updateGoal(@PathVariable UUID id, @Valid @RequestBody UpdateGoalRequest request) {
		Goal goal = goals.findById(id)
				.orElseThrow(() -> new GoalNotFoundException(id));

		goal.update(request.title(), request.description());
		Goal savedGoal = goals.save(goal);

		return GoalResponse.from(savedGoal);
	}

	@PostMapping("/{id}/start")
	GoalResponse startGoal(@PathVariable UUID id) {
		Goal goal = goals.findById(id)
				.orElseThrow(() -> new GoalNotFoundException(id));

		goal.start();
		Goal savedGoal = goals.save(goal);

		return GoalResponse.from(savedGoal);
	}

	@PostMapping("/{id}/complete")
	GoalResponse completeGoal(@PathVariable UUID id) {
		Goal goal = goals.findById(id)
				.orElseThrow(() -> new GoalNotFoundException(id));

		goal.complete();
		Goal savedGoal = goals.save(goal);

		return GoalResponse.from(savedGoal);
	}
}
