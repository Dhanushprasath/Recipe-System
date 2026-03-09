package com.recipe.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.GeneratedValue;




import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "diet_logs")
@Getter
@Setter
@NoArgsConstructor
public class DietLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;



    private String meal; // Breakfast, Lunch, Dinner

    private String foodItem; // Food item description

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // User who created the log
}
