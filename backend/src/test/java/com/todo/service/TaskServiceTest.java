package com.todo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.todo.BaseDbTest;
import com.todo.api.TaskRequest;
import com.todo.domain.Task;

class TaskServiceTest extends BaseDbTest {

    @Test
    void createWithValidTitleStoresPendingTaskWithTimestamps() {
        Task task = service.create(new TaskRequest("Comprar leite", "2 litros", null));

        assertThat(task.id()).isNotNull();
        assertThat(task.title()).isEqualTo("Comprar leite");
        assertThat(task.description()).isEqualTo("2 litros");
        assertThat(task.status()).isEqualTo(TaskService.STATUS_PENDING);
        assertThat(task.createdAt()).isPositive();
        assertThat(task.updatedAt()).isEqualTo(task.createdAt());
    }

    @Test
    void createWithBlankNullOrSpacesOnlyTitleFallsBackToDefaultTitle() {
        Task blank = service.create(new TaskRequest("", "d", null));
        Task whitespace = service.create(new TaskRequest("   ", "d", null));
        Task absent = service.create(new TaskRequest(null, "d", null));

        assertThat(blank.title()).isEqualTo(TaskService.DEFAULT_TITLE);
        assertThat(whitespace.title()).isEqualTo(TaskService.DEFAULT_TITLE);
        assertThat(absent.title()).isEqualTo(TaskService.DEFAULT_TITLE);
    }

    @Test
    void createWithTitleOverLimitRejects() {
        assertThatThrownBy(() -> service.create(new TaskRequest("a".repeat(TaskService.MAX_TITLE_LENGTH + 1), null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(TaskService.MAX_TITLE_LENGTH));
    }

    @Test
    void findAllReturnsEmptyListWhenNoTasks() {
        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void findAllReturnsDeterministicOrderPendingFirstThenNewest() {
        repository.insert(new Task(null, "antiga-pendente", null, TaskService.STATUS_PENDING, 1000, 1000));
        repository.insert(new Task(null, "nova-pendente", null, TaskService.STATUS_PENDING, 2000, 2000));
        repository.insert(new Task(null, "concluida", null, TaskService.STATUS_COMPLETED, 3000, 3000));

        List<Task> tasks = service.findAll();

        assertThat(tasks).extracting(Task::title)
                .containsExactly("nova-pendente", "antiga-pendente", "concluida");
    }

    @Test
    void updateAppliesPartialChangesAndPreservesCreationFields() {
        Task created = service.create(new TaskRequest("Título", "desc original", null));

        Task updated = service.update(created.id(),
                new TaskRequest("Novo título", null, null));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.title()).isEqualTo("Novo título");
        assertThat(updated.description()).isEqualTo("desc original");
        assertThat(updated.status()).isEqualTo(created.status());
        assertThat(updated.createdAt()).isEqualTo(created.createdAt());
        assertThat(updated.updatedAt()).isGreaterThanOrEqualTo(created.updatedAt());
    }

    @Test
    void updateToggleStatusKeepsOtherFieldsAndRefreshesUpdatedAt() {
        Task created = service.create(new TaskRequest("Só status", null, null));

        Task pending = service.update(created.id(), new TaskRequest(null, null, TaskService.STATUS_PENDING));
        Task completed = service.update(created.id(), new TaskRequest(null, null, TaskService.STATUS_COMPLETED));
        Task backToPending = service.update(created.id(), new TaskRequest(null, null, TaskService.STATUS_PENDING));

        assertThat(pending.status()).isEqualTo(TaskService.STATUS_PENDING);
        assertThat(completed.status()).isEqualTo(TaskService.STATUS_COMPLETED);
        assertThat(backToPending.status()).isEqualTo(TaskService.STATUS_PENDING);
        assertThat(backToPending.updatedAt()).isGreaterThanOrEqualTo(created.updatedAt());
    }

    @Test
    void updateWithBlankTitleFallsBackToDefaultTitle() {
        Task created = service.create(new TaskRequest("Título", null, null));

        Task updated = service.update(created.id(), new TaskRequest("   ", "nova desc", null));

        assertThat(updated.title()).isEqualTo(TaskService.DEFAULT_TITLE);
        assertThat(updated.description()).isEqualTo("nova desc");
    }

    @Test
    void updateWithInvalidStatusRejects() {
        Task created = service.create(new TaskRequest("Título", null, null));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.update(created.id(), new TaskRequest(null, null, "done")))
                .withMessageContaining("Status inválido");
    }

    @Test
    void updateMissingTaskReturnsNotFound() {
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.update(999L, new TaskRequest("x", null, null)))
                .satisfies(ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteRemovesTaskThenMissingDeleteReturnsNotFound() {
        Task created = service.create(new TaskRequest("Apagar", null, null));

        service.delete(created.id());

        assertThat(repository.findById(created.id())).isEmpty();
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.delete(created.id()))
                .satisfies(ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void findByIdMissingReturnsNotFound() {
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.findById(404L))
                .satisfies(ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}