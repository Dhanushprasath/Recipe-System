package com.recipe.backend.repository;

import com.recipe.backend.model.DietLog;
import com.recipe.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DietLogRepository extends JpaRepository<DietLog, Long> {

    List<DietLog> findByUserOrderByDateDesc(User user);

    List<DietLog> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate start, LocalDate end);
}
