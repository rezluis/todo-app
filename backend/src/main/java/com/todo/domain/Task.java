package com.todo.domain;

public record Task(
        Long id,
        String title,
        String description,
        String status,
        long createdAt,
        long updatedAt) {
}