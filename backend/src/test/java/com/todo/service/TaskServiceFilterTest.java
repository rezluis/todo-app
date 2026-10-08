package com.todo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.todo.BaseDbTest;
import com.todo.api.TaskRequest;
import com.todo.domain.Task;

class TaskServiceFilterTest extends BaseDbTest {
    
    @Test
    void findAllWithoutFilterReturnsAllTasks() {
        repository.insert(new Task(null, "todo1", null, "TODO", 1000, 1000));
        repository.insert(new Task(null, "inprog1", null, "IN_PROGRESS", 2000, 2000));
        repository.insert(new Task(null, "done1", null, "DONE", 3000, 3000));
        
        List<Task> tasks = service.findAll(null);
        assertThat(tasks).extracting(Task::title)
                .containsExactly("inprog1", "todo1", "done1");
    }
    
    @Test
    void findAllWithTodoReturnsOnlyTodoTasks() {
        repository.insert(new Task(null, "todo1", null, "TODO", 1000, 1000));
        repository.insert(new Task(null, "todo2", null, "TODO", 2000, 2000));
        repository.insert(new Task(null, "done1", null, "DONE", 3000, 3000));
        repository.insert(new Task(null, "inprog1", null, "IN_PROGRESS", 4000, 4000));
        
        List<Task> tasks = service.findAll("TODO");
        assertThat(tasks).hasSize(2);
        assertThat(tasks).allMatch(t -> "TODO".equals(t.status()));
        assertThat(tasks).extracting(Task::title)
                .containsExactly("todo2", "todo1");
    }
    
    @Test
    void findAllWithInProgressReturnsOnlyInProgressTasks() {
        repository.insert(new Task(null, "todo1", null, "TODO", 1000, 1000));
        repository.insert(new Task(null, "inprog1", null, "IN_PROGRESS", 2000, 2000));
        repository.insert(new Task(null, "inprog2", null, "IN_PROGRESS", 3000, 3000));
        repository.insert(new Task(null, "done1", null, "DONE", 4000, 4000));
        
        List<Task> tasks = service.findAll("IN_PROGRESS");
        assertThat(tasks).hasSize(2);
        assertThat(tasks).allMatch(t -> "IN_PROGRESS".equals(t.status()));
        assertThat(tasks).extracting(Task::title)
                .containsExactly("inprog2", "inprog1");
    }
    
    @Test
    void findAllWithDoneReturnsOnlyDoneTasks() {
        repository.insert(new Task(null, "todo1", null, "TODO", 1000, 1000));
        repository.insert(new Task(null, "inprog1", null, "IN_PROGRESS", 2000, 2000));
        repository.insert(new Task(null, "done1", null, "DONE", 3000, 3000));
        repository.insert(new Task(null, "done2", null, "DONE", 4000, 4000));
        repository.insert(new Task(null, "done3", null, "DONE", 5000, 5000));
        
        List<Task> tasks = service.findAll("DONE");
        assertThat(tasks).hasSize(3);
        assertThat(tasks).allMatch(t -> "DONE".equals(t.status()));
        assertThat(tasks).extracting(Task::title)
                .containsExactly("done3", "done2", "done1");
    }
    
    @Test
    void findAllWithInvalidStatusThrowsIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.findAll("INVALID"))
                .withMessageContaining("Status inválido");
    }
    
    @Test
    void findAllWithLowercaseStatusIsRejected() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.findAll("todo"));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.findAll("done"));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.findAll("in_progress"));
    }
    
    @Test
    void findAllWithStatusWithSpacesIsRejected() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.findAll(" TODO "));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.findAll(" DONE"));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.findAll("TODO "));
    }
}