package com.todo.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.todo.BaseDbTest;
import com.todo.domain.Task;

@AutoConfigureMockMvc
class TaskControllerFilterTest extends BaseDbTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getWithoutFilterReturnsAllTasks() throws Exception {
        repository.insert(new Task(null, "todo1", null, "TODO", 1000, 1000));
        repository.insert(new Task(null, "inprog1", null, "IN_PROGRESS", 2000, 2000));
        repository.insert(new Task(null, "done1", null, "DONE", 3000, 3000));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getWithStatusTodoReturnsOnlyTodo() throws Exception {
        repository.insert(new Task(null, "todo1", null, "TODO", 1000, 1000));
        repository.insert(new Task(null, "inprog1", null, "IN_PROGRESS", 2000, 2000));
        repository.insert(new Task(null, "done1", null, "DONE", 3000, 3000));

        mockMvc.perform(get("/api/tasks").param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[0].title").value("todo1"));
    }

    @Test
    void getWithStatusInProgressReturnsOnlyInProgress() throws Exception {
        repository.insert(new Task(null, "todo1", null, "TODO", 1000, 1000));
        repository.insert(new Task(null, "inprog1", null, "IN_PROGRESS", 2000, 2000));
        repository.insert(new Task(null, "inprog2", null, "IN_PROGRESS", 3000, 3000));
        repository.insert(new Task(null, "done1", null, "DONE", 4000, 4000));

        mockMvc.perform(get("/api/tasks").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[1].status").value("IN_PROGRESS"));
    }

    @Test
    void getWithStatusDoneReturnsOnlyDone() throws Exception {
        repository.insert(new Task(null, "todo1", null, "TODO", 1000, 1000));
        repository.insert(new Task(null, "inprog1", null, "IN_PROGRESS", 2000, 2000));
        repository.insert(new Task(null, "done1", null, "DONE", 3000, 3000));

        mockMvc.perform(get("/api/tasks").param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("DONE"));
    }

    @Test
    void getWithInvalidStatusReturns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getWithStatusFilterExcludesTasksOfOtherStatuses() throws Exception {
        repository.insert(new Task(null, "todo1", null, "TODO", 1000, 1000));
        repository.insert(new Task(null, "todo2", null, "TODO", 2000, 2000));
        repository.insert(new Task(null, "todo3", null, "TODO", 3000, 3000));
        repository.insert(new Task(null, "done1", null, "DONE", 4000, 4000));

        mockMvc.perform(get("/api/tasks").param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[1].status").value("TODO"))
                .andExpect(jsonPath("$[2].status").value("TODO"));
    }

    @Test
    void getWithLowercaseStatusReturns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "todo"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/tasks").param("status", "done"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/tasks").param("status", "in_progress"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWithStatusWithSpacesReturns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", " TODO "))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/tasks").param("status", " TODO"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/tasks").param("status", "TODO "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWithMultipleStatusParamsReturnsResultsBasedOnFirst() throws Exception {
        repository.insert(new Task(null, "todo1", null, "TODO", 1000, 1000));
        repository.insert(new Task(null, "done1", null, "DONE", 2000, 2000));

        mockMvc.perform(get("/api/tasks")
                .param("status", "TODO")
                .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("TODO"));
    }
}