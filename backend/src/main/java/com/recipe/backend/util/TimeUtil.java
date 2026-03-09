
package com.recipe.backend.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtil {

    // ✅ MUST be static
    public static String getTimeAgo(LocalDateTime createdAt) {

        Duration duration = Duration.between(createdAt, LocalDateTime.now());

        if (duration.toMinutes() < 1) return "Just now";
        if (duration.toMinutes() < 60) return duration.toMinutes() + " min ago";
        if (duration.toHours() < 24) return duration.toHours() + " hr ago";
        if (duration.toDays() < 7) return duration.toDays() + " days ago";

        return createdAt.toLocalDate().toString();
    }
}
