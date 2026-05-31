package de.bennisdurchstarterprogramm.learningtracker.goal;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GoalApiTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private GoalRepository goals;

	@BeforeEach
	void clearGoals() {
		goals.deleteAll();
	}

	@Test
	void createsGoal() throws Exception {
		mvc.perform(post("/goals")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Build first backend slice",
								  "description": "Expose the first Goal API."
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(".*/goals/[0-9a-f-]+")))
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.title").value("Build first backend slice"))
				.andExpect(jsonPath("$.description").value("Expose the first Goal API."))
				.andExpect(jsonPath("$.status").value("TODO"));
	}

	@Test
	void listsGoals() throws Exception {
		goals.save(new Goal("Containerize the service", null));

		mvc.perform(get("/goals"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").isNotEmpty())
				.andExpect(jsonPath("$[0].title").value("Containerize the service"))
				.andExpect(jsonPath("$[0].description").doesNotExist())
				.andExpect(jsonPath("$[0].status").value("TODO"));
	}

	@Test
	void getsGoalById() throws Exception {
		Goal savedGoal = goals.save(new Goal("Read the Goal API", "Understand the lookup endpoint."));

		mvc.perform(get("/goals/{id}", savedGoal.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedGoal.getId().toString()))
				.andExpect(jsonPath("$.title").value("Read the Goal API"))
				.andExpect(jsonPath("$.description").value("Understand the lookup endpoint."))
				.andExpect(jsonPath("$.status").value("TODO"));
	}

	@Test
	void returnsNotFoundForUnknownGoal() throws Exception {
		UUID unknownGoalId = UUID.fromString("00000000-0000-0000-0000-000000000001");

		mvc.perform(get("/goals/{id}", unknownGoalId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Resource not found"))
				.andExpect(jsonPath("$.detail").value("Goal not found."))
				.andExpect(jsonPath("$.goalId").value(unknownGoalId.toString()));
	}

	@Test
	void updatesGoal() throws Exception {
		Goal savedGoal = goals.save(new Goal("Read about controllers", "Understand GET endpoints."));

		mvc.perform(put("/goals/{id}", savedGoal.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Read about updates",
								  "description": "Understand PUT endpoints."
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedGoal.getId().toString()))
				.andExpect(jsonPath("$.title").value("Read about updates"))
				.andExpect(jsonPath("$.description").value("Understand PUT endpoints."))
				.andExpect(jsonPath("$.status").value("TODO"));

		mvc.perform(get("/goals/{id}", savedGoal.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Read about updates"))
				.andExpect(jsonPath("$.description").value("Understand PUT endpoints."))
				.andExpect(jsonPath("$.status").value("TODO"));
	}

	@Test
	void returnsNotFoundWhenUpdatingUnknownGoal() throws Exception {
		UUID unknownGoalId = UUID.fromString("00000000-0000-0000-0000-000000000002");

		mvc.perform(put("/goals/{id}", unknownGoalId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Updated title"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Resource not found"))
				.andExpect(jsonPath("$.detail").value("Goal not found."))
				.andExpect(jsonPath("$.goalId").value(unknownGoalId.toString()));
	}

	@Test
	void rejectsBlankTitleWhenUpdatingGoal() throws Exception {
		Goal savedGoal = goals.save(new Goal("Read about validation", null));

		mvc.perform(put("/goals/{id}", savedGoal.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": " "
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid request"))
				.andExpect(jsonPath("$.detail").value("Request validation failed."))
				.andExpect(jsonPath("$.errors.title").value("title must not be blank"));
	}

	@Test
	void startsGoal() throws Exception {
		Goal savedGoal = goals.save(new Goal("Start learning Spring", null));

		mvc.perform(post("/goals/{id}/start", savedGoal.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedGoal.getId().toString()))
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));

		mvc.perform(get("/goals/{id}", savedGoal.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));
	}

	@Test
	void completesGoal() throws Exception {
		Goal savedGoal = goals.save(new Goal("Finish first CRUD slice", null));
		savedGoal.start();
		goals.save(savedGoal);

		mvc.perform(post("/goals/{id}/complete", savedGoal.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedGoal.getId().toString()))
				.andExpect(jsonPath("$.status").value("DONE"));

		mvc.perform(get("/goals/{id}", savedGoal.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DONE"));
	}

	@Test
	void rejectsCompletingGoalBeforeItIsStarted() throws Exception {
		Goal savedGoal = goals.save(new Goal("Finish after starting", null));

		mvc.perform(post("/goals/{id}/complete", savedGoal.getId()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Invalid goal status transition"))
				.andExpect(jsonPath("$.detail").value("Goal status transition is not allowed."))
				.andExpect(jsonPath("$.goalId").value(savedGoal.getId().toString()))
				.andExpect(jsonPath("$.currentStatus").value("TODO"))
				.andExpect(jsonPath("$.targetStatus").value("DONE"));
	}

	@Test
	void rejectsStartingCompletedGoal() throws Exception {
		Goal savedGoal = goals.save(new Goal("Do not restart completed goals", null));
		savedGoal.start();
		savedGoal.complete();
		goals.save(savedGoal);

		mvc.perform(post("/goals/{id}/start", savedGoal.getId()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Invalid goal status transition"))
				.andExpect(jsonPath("$.detail").value("Goal status transition is not allowed."))
				.andExpect(jsonPath("$.goalId").value(savedGoal.getId().toString()))
				.andExpect(jsonPath("$.currentStatus").value("DONE"))
				.andExpect(jsonPath("$.targetStatus").value("IN_PROGRESS"));
	}

	@Test
	void returnsNotFoundWhenStartingUnknownGoal() throws Exception {
		UUID unknownGoalId = UUID.fromString("00000000-0000-0000-0000-000000000003");

		mvc.perform(post("/goals/{id}/start", unknownGoalId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Resource not found"))
				.andExpect(jsonPath("$.detail").value("Goal not found."))
				.andExpect(jsonPath("$.goalId").value(unknownGoalId.toString()));
	}

	@Test
	void returnsNotFoundWhenCompletingUnknownGoal() throws Exception {
		UUID unknownGoalId = UUID.fromString("00000000-0000-0000-0000-000000000004");

		mvc.perform(post("/goals/{id}/complete", unknownGoalId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Resource not found"))
				.andExpect(jsonPath("$.detail").value("Goal not found."))
				.andExpect(jsonPath("$.goalId").value(unknownGoalId.toString()));
	}

	@Test
	void rejectsBlankTitle() throws Exception {
		mvc.perform(post("/goals")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": " "
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid request"))
				.andExpect(jsonPath("$.detail").value("Request validation failed."))
				.andExpect(jsonPath("$.errors.title").value("title must not be blank"));
	}
}
