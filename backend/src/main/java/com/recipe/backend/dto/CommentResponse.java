
package com.recipe.backend.dto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class CommentResponse {

    private Long id;
    private String content;

    private Long userId;
    private String username;
    private boolean isAdmin;
    private boolean isOwner; // 👈 "You"

    private LocalDateTime createdAt;

    private List<CommentResponse> replies;
    private String timeAgo;
    private Long recipeId;
}
