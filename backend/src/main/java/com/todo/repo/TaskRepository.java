package com.todo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.todo.domain.Task;

@Repository
public class TaskRepository {

    private static final RowMapper<Task> ROW_MAPPER = (rs, rowNum) -> new Task(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("status"),
            rs.getLong("created_at"),
            rs.getLong("updated_at"));

    private final JdbcTemplate jdbc;

    public TaskRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(Task task) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO tasks (title, description, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                    new String[] { "id" });
            ps.setString(1, task.title());
            ps.setString(2, task.description());
            ps.setString(3, task.status());
            ps.setLong(4, task.createdAt());
            ps.setLong(5, task.updatedAt());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<Task> findAll() {
        return jdbc.query(
                "SELECT * FROM tasks ORDER BY (status = 'completed') ASC, created_at DESC, id DESC",
                ROW_MAPPER);
    }

    public Optional<Task> findById(long id) {
        return jdbc.query("SELECT * FROM tasks WHERE id = ?", ROW_MAPPER, id).stream().findFirst();
    }

    public int update(Task task) {
        return jdbc.update(
                "UPDATE tasks SET title = ?, description = ?, status = ?, updated_at = ? WHERE id = ?",
                task.title(), task.description(), task.status(), task.updatedAt(), task.id());
    }

    public int delete(long id) {
        return jdbc.update("DELETE FROM tasks WHERE id = ?", id);
    }
}