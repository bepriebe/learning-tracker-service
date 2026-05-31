package de.bennisdurchstarterprogramm.learningtracker.learninglog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LearningLogApiTests {

    @Autowired
    private MockMvc mvc;

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
}
