package com.recipe.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    USER,
    ADMIN,
    BANNED;

    @JsonCreator
    public static Role fromValue(String value) {
        return Role.valueOf(value.toUpperCase());
    }
}
