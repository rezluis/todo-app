package com.todo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.todo.api.TaskRequest;
import com.todo.domain.Task;
import com.todo.repo.TaskRepository;

@Service
public class TaskService {

    public static final int MAX_TITLE_LENGTH = 120;
    public static final String STATUS_TODO = "TODO";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_DONE = "DONE";

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task create(TaskRequest request) {
        String title = normalizeTitle(request.title());
        String description = request.description();
        long now = System.currentTimeMillis();
        long id = repository.insert(new Task(null, title, description, STATUS_TODO, now, now));
        return repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Falha ao gravar a tarefa."));
    }

    public List<Task> findAll() {
        return findAll(null);
    }

    public List<Task> findAll(String status) {
        if (status == null) {
            return repository.findAll();
        }
        return repository.findAllByStatus(validateStatus(status));
    }

    public Task findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada."));
    }

    public Task update(long id, TaskRequest request) {
        Task existing = findById(id);
        String title = request.title() != null ? normalizeTitle(request.title()) : existing.title();
        String description = request.description() != null ? request.description() : existing.description();
        String status = request.status() != null ? validateStatus(request.status()) : existing.status();
        Task updated = new Task(id, title, description, status, existing.createdAt(), System.currentTimeMillis());
        repository.update(updated);
        return updated;
    }

    public void delete(long id) {
        if (repository.delete(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada.");
        }
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório.");
        }
        String trimmed = title.strip();
        if (trimmed.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Título excede o limite de " + MAX_TITLE_LENGTH + " caracteres.");
        }
        return trimmed;
    }

    private String validateStatus(String status) {
        if (!STATUS_TODO.equals(status) && !STATUS_IN_PROGRESS.equals(status) && !STATUS_DONE.equals(status)) {
            throw new IllegalArgumentException("Status inválido. Use 'TODO', 'IN_PROGRESS' ou 'DONE'.");
        }
        return status;
    }
}