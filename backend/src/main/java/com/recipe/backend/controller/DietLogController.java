package com.recipe.backend.controller;

import com.recipe.backend.model.DietLog;
import com.recipe.backend.model.User;
import com.recipe.backend.service.DietLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/diet")
@RequiredArgsConstructor
public class DietLogController {

    private final DietLogService dietLogService;


@PostMapping("/add")
public ResponseEntity<?> addDietLog(@RequestBody DietLog dietLog, Authentication auth) {
    try {
        String email = auth.getName(); // get logged-in email
        User user = dietLogService.getUserByEmail(email); // fetch actual User entity
        dietLog.setUser(user); // set user in diet log
        DietLog saved = dietLogService.saveOrUpdate(dietLog, user);
        return ResponseEntity.ok(saved);
    } catch (Exception e) {
        e.printStackTrace(); // shows exact error in backend console
        return ResponseEntity.status(500).body("Error saving diet log: " + e.getMessage());
    }
}


    @GetMapping("/all")
    public ResponseEntity<List<DietLog>> getAllLogs(Authentication auth) {
        String email = auth.getName();
        User user = dietLogService.getUserByEmail(email);
        return ResponseEntity.ok(dietLogService.getAllLogs(user));
    }


    @GetMapping("/weekly")
    public ResponseEntity<List<DietLog>> getWeeklyLogs(Authentication auth) {
        String email = auth.getName();
        User user = dietLogService.getUserByEmail(email);
        return ResponseEntity.ok(dietLogService.getWeeklyLogs(user));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<DietLog>> getMonthlyLogs(Authentication auth) {
        String email = auth.getName();
        User user = dietLogService.getUserByEmail(email);
        return ResponseEntity.ok(dietLogService.getMonthlyLogs(user));
    }

}








