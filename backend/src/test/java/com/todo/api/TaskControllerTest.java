package com.todo;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.todo.api.TaskRequest;
import com.todo.domain.Task;
import com.todo.service.TaskService;

@AutoConfigureMockMvc
class TaskControllerTest extends BaseDbTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postCreatesTaskAndReturnsCreatedWithContractShape() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Comprar leite\",\"description\":\"2 litros\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Comprar leite"))
                .andExpect(jsonPath("$.description").value("2 litros"))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.createdAt").isNumber())
                .andExpect(jsonPath("$.updatedAt").isNumber());
    }

    @Test
    void postWithBlankTitleFallsBackToDefaultTitle() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(TaskService.DEFAULT_TITLE));
    }

    @Test
    void postWithTitleOverLimitRejectsWithBadRequest() throws Exception {
        String longTitle = "a".repeat(TaskService.MAX_TITLE_LENGTH + 1);
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + longTitle + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("120")));
    }

    @Test
    void postWithMalformedJsonRejects() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nao-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getReturnsOrderedListAndEmptyListWhenNoTasks() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        repository.insert(new Task(null, "antiga-pendente", null, TaskService.STATUS_PENDING, 1000, 1000));
        repository.insert(new Task(null, "nova-pendente", null, TaskService.STATUS_PENDING, 2000, 2000));
        repository.insert(new Task(null, "concluida", null, TaskService.STATUS_COMPLETED, 3000, 3000));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title").value("nova-pendente"))
                .andExpect(jsonPath("$[1].title").value("antiga-pendente"))
                .andExpect(jsonPath("$[2].title").value("concluida"));
    }

    @Test
    void getByIdReturnsTaskOrNotFound() throws Exception {
        Task created = service.create(new TaskRequest("Específica", null, null));

        mockMvc.perform(get("/api/tasks/{id}", created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id()));

        mockMvc.perform(get("/api/tasks/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void putTogglesStatusAndRejectsInvalidStatus() throws Exception {
        Task created = service.create(new TaskRequest("Toggle", null, null));

        mockMvc.perform(put("/api/tasks/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"completed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"));

        mockMvc.perform(put("/api/tasks/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"pending\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("pending"));

        mockMvc.perform(put("/api/tasks/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"done\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Status inválido")));
    }

    @Test
    void putUpdatesTitleAndDescription() throws Exception {
        Task created = service.create(new TaskRequest("Antes", "d1", null));

        mockMvc.perform(put("/api/tasks/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Depois\",\"description\":\"d2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Depois"))
                .andExpect(jsonPath("$.description").value("d2"))
                .andExpect(jsonPath("$.id").value(created.id()));
    }

    @Test
    void putWithBlankTitleAndLongTitleHandled() throws Exception {
        Task created = service.create(new TaskRequest("Antes", null, null));

        mockMvc.perform(put("/api/tasks/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(TaskService.DEFAULT_TITLE));

        String longTitle = "b".repeat(TaskService.MAX_TITLE_LENGTH + 1);
        mockMvc.perform(put("/api/tasks/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + longTitle + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putOnMissingTaskReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/tasks/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesAndReturnsNoContentThenNotFound() throws Exception {
        Task created = service.create(new TaskRequest("Apagar", null, null));

        mockMvc.perform(delete("/api/tasks/{id}", created.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/tasks/{id}", created.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownRouteReturnsNotFoundNotInternalError() throws Exception {
        mockMvc.perform(get("/api/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}