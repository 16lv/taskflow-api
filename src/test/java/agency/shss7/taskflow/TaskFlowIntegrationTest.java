package agency.shss7.taskflow;

import agency.shss7.taskflow.domain.Role;
import agency.shss7.taskflow.domain.TaskPriority;
import agency.shss7.taskflow.domain.TaskStatus;
import agency.shss7.taskflow.dto.AuthDtos.*;
import agency.shss7.taskflow.dto.ProjectDtos.*;
import agency.shss7.taskflow.dto.TaskDtos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthEndpointReturnsUpStatusWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("taskflow-api"))
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void fullProjectAndTaskLifecycleWithJwtAndStateValidation() throws Exception {
        // 1. Register user
        var registerReq = new RegisterRequest(
            "engineer@cardiff.ac.uk",
            "Cardiff Engineer",
            "SecurePass123!",
            Role.MAINTAINER
        );

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andReturn();

        AuthResponse auth = objectMapper.readValue(
            regResult.getResponse().getContentAsString(),
            AuthResponse.class
        );
        String bearer = "Bearer " + auth.accessToken();

        // 2. Create project
        var projectReq = new CreateProjectRequest(
            "CORE-API",
            "Core Platform API Migration",
            "Migrate legacy endpoints to Spring Boot 3 with OpenAPI contracts"
        );

        MvcResult projResult = mockMvc.perform(post("/api/v1/projects")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("CORE-API"))
            .andReturn();

        ProjectResponse project = objectMapper.readValue(
            projResult.getResponse().getContentAsString(),
            ProjectResponse.class
        );

        // 3. Create task in project
        var taskReq = new CreateTaskRequest(
            project.id(),
            "Implement RFC 7807 ProblemDetail error responses",
            "Standardize validation and state-transition errors across all controllers",
            TaskPriority.HIGH,
            LocalDate.now().plusDays(7),
            auth.userId()
        );

        MvcResult taskResult = mockMvc.perform(post("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("BACKLOG"))
            .andExpect(jsonPath("$.priority").value("HIGH"))
            .andReturn();

        TaskResponse task = objectMapper.readValue(
            taskResult.getResponse().getContentAsString(),
            TaskResponse.class
        );

        // 4. Reject illegal direct transition from BACKLOG -> DONE
        var invalidTransition = new StatusTransitionRequest(TaskStatus.DONE);
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", task.id())
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTransition)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.title").value("Invalid Workflow State Transition"));

        // 5. Valid transition BACKLOG -> IN_PROGRESS -> DONE
        var toInProgress = new StatusTransitionRequest(TaskStatus.IN_PROGRESS);
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", task.id())
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toInProgress)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        var toDone = new StatusTransitionRequest(TaskStatus.DONE);
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", task.id())
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toDone)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DONE"));

        // 6. Query filtered tasks and verify project completion count
        mockMvc.perform(get("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .param("projectId", project.id().toString())
                .param("status", "DONE")
                .param("q", "ProblemDetail"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/v1/projects/CORE-API")
                .header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completedTasks").value(1));
    }

    @Test
    void unauthenticatedRequestsToProtectedEndpointsReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
            .andExpect(status().isUnauthorized());
    }
}
