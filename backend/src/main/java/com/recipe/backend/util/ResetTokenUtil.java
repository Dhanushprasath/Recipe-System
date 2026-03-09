

package com.recipe.backend.util;

import java.util.UUID;

public class ResetTokenUtil {

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}

