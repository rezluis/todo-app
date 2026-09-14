package com.todo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.todo.repo.TaskRepository;
import com.todo.service.TaskService;

@SpringBootTest
public abstract class BaseDbTest {

    @DynamicPropertySource
    static void dbProperties(DynamicPropertyRegistry registry) throws IOException {
        Path dbFile = Files.createTempFile("todo-test-", ".db");
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + dbFile.toAbsolutePath());
    }

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected TaskRepository repository;

    @Autowired
    protected TaskService service;

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM tasks");
    }
}