
package com.recipe.backend.dto;

import lombok.Data;

@Data
public class CommentRequest {

    private String content;
    private Long parentId; // null for normal comment, value for reply
}
