package com.recipe.backend.dto;

public record UserDto(Long id, String username, String email, com.recipe.backend.model.Role role) {}
