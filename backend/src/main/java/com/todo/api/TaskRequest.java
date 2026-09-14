package com.todo.api;

public record TaskRequest(String title, String description, String status) {
}