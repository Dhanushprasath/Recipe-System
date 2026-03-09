package com.recipe.backend.service;

import com.recipe.backend.model.DietLog;
import com.recipe.backend.model.User;
import java.util.List;

public interface DietLogService {

    DietLog saveOrUpdate(DietLog dietLog, User user);

    List<DietLog> getAllLogs(User user);

    List<DietLog> getWeeklyLogs(User user);

    List<DietLog> getMonthlyLogs(User user);
    User getUserByEmail(String email);
}
