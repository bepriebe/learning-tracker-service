package de.bennisdurchstarterprogramm.learningtracker.learninglog;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
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
class LearningLogApiTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private LearningLogRepository learningLogs;

    @BeforeEach
	void clearLearningLogs() {
		learningLogs.deleteAll();
	}

	@AfterEach
	void removeLearningLogs() {
		learningLogs.deleteAll();
	}

	@Test
	void createsLearningLog() throws Exception {
        mvc.perform(post("/learning-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {
                                    "topic": "Docker Compose",
                                    "summary": "Healthchecks and volumes understood.",
                                    "nextStep": "Explain compose.yaml in my own words."
                                  }
                                  """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(".*/learning-logs/[0-9a-f-]+")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.topic").value("Docker Compose"))
				.andExpect(jsonPath("$.summary").value("Healthchecks and volumes understood."))
				.andExpect(jsonPath("$.nextStep").value("Explain compose.yaml in my own words."));
	}

	@Test
	void listsLearningLogs() throws Exception {
        learningLogs.save(new LearningLog("Docker Compose", "Healthchecks and volumes understood.", null));

        mvc.perform(get("/learning-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].topic").value("Docker Compose"))
                .andExpect(jsonPath("$[0].summary").value("Healthchecks and volumes understood."))
				.andExpect(jsonPath("$[0].nextStep").doesNotExist());
	}

	@Test
	void createsLearningLogLinkedToGoal() throws Exception {
		String goalResponse = mvc.perform(post("/goals")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Understand container orchestration"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String goalId = JsonPath.read(goalResponse, "$.id");

		mvc.perform(post("/learning-logs")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "topic": "Docker Compose",
								  "summary": "Connected a learning log to a goal.",
								  "goalId": "%s"
								}
								""".formatted(goalId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.goalId").value(goalId));
	}

	@Test
	void returnsNotFoundForUnknownGoal() throws Exception {
		String unknownGoalId = "00000000-0000-0000-0000-000000000005";

		mvc.perform(post("/learning-logs")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "topic": "Docker Compose",
								  "summary": "Try to link an unknown goal.",
								  "goalId": "%s"
								}
								""".formatted(unknownGoalId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.goalId").value(unknownGoalId));
	}
}
