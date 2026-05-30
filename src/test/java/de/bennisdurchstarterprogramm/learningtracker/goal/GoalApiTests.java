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
				.andExpect(jsonPath("$.description").value("Expose the first Goal API."));
	}

	@Test
	void listsGoals() throws Exception {
		goals.save(new Goal("Containerize the service", null));

		mvc.perform(get("/goals"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").isNotEmpty())
				.andExpect(jsonPath("$[0].title").value("Containerize the service"))
				.andExpect(jsonPath("$[0].description").doesNotExist());
	}

	@Test
	void getsGoalById() throws Exception {
		Goal savedGoal = goals.save(new Goal("Read the Goal API", "Understand the lookup endpoint."));

		mvc.perform(get("/goals/{id}", savedGoal.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedGoal.getId().toString()))
				.andExpect(jsonPath("$.title").value("Read the Goal API"))
				.andExpect(jsonPath("$.description").value("Understand the lookup endpoint."));
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
