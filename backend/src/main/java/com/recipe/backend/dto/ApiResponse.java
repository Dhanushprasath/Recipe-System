
package com.recipe.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiResponse {

    // setter
    // getter
    private String message;

    // constructor
    public ApiResponse(String message) {
        this.message = message;
    }

}

