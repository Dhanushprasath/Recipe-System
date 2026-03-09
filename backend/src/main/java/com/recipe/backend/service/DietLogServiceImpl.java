package com.recipe.backend.service;

import com.recipe.backend.model.DietLog;
import com.recipe.backend.model.User;
import com.recipe.backend.repository.DietLogRepository;
import com.recipe.backend.repository.UserRepository;
import com.recipe.backend.service.DietLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DietLogServiceImpl implements DietLogService {

    private final DietLogRepository dietLogRepository;
    private final UserRepository userRepository;
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public DietLog saveOrUpdate(DietLog dietLog, User user) {
        if (dietLog.getId() != null) {
            // Update existing log
            DietLog existing = dietLogRepository.findById(dietLog.getId())
                    .orElseThrow(() -> new RuntimeException("DietLog not found with id: " + dietLog.getId()));
            existing.setDate(dietLog.getDate());
            existing.setMeal(dietLog.getMeal());
            existing.setFoodItem(dietLog.getFoodItem());
            return dietLogRepository.save(existing);
        } else {
            // Create new log
            dietLog.setUser(user);
            return dietLogRepository.save(dietLog);
        }
    }

    @Override
    public List<DietLog> getAllLogs(User user) {
        return dietLogRepository.findByUserOrderByDateDesc(user);
    }

    @Override
    public List<DietLog> getWeeklyLogs(User user) {
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate weekEnd = LocalDate.now().with(DayOfWeek.SUNDAY);
        return dietLogRepository.findByUserAndDateBetweenOrderByDateDesc(user, weekStart, weekEnd);
    }

    @Override
    public List<DietLog> getMonthlyLogs(User user) {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        return dietLogRepository.findByUserAndDateBetweenOrderByDateDesc(user, monthStart, monthEnd);
    }
}
