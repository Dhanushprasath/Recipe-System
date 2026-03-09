
package com.recipe.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "recipe")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition="TEXT")
    private String description;

    private Integer servings;

    @Column(nullable = false)
    private double totalCalories;

    @Column(nullable = false)
    private double totalProtein;

    @Column(nullable = false)
    private double totalCarbs;

    @Column(nullable = false)
    private double totalFats;


    @Column(name = "cooking_time", nullable = false)
    private Integer cookingTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonBackReference  // ✅ stops recursion when serializing Recipe -> User
    private User createdBy;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @ToString.Exclude
    @Builder.Default
    @Column(columnDefinition="TEXT")
    private Set<Ingredient> ingredients = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private Boolean approved = false;

    @Builder.Default
    private Boolean rejected = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();




    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();


}

