package com.todo.api;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.todo.domain.Task;
import com.todo.service.TaskService;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final String STATUS_PARAM = "status";

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<Task> list(HttpServletRequest request) {
        // Issue #8: lê os valores crus de ?status sem a conversão do Spring
        // (@RequestParam trima/separa), pois a validação é case-sensitive e sem
        // normalização; com múltiplos valores, usa apenas o primeiro.
        String[] values = request.getParameterValues(STATUS_PARAM);
        String status = values != null && values.length > 0 ? values[0] : null;
        return service.findAll(status);
    }

    @GetMapping("/{id}")
    public Task get(@PathVariable long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Task> create(@RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable long id, @RequestBody TaskRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}