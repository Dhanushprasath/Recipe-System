//package com.recipe.backend.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.*;
//
//@Entity
//@Table(name = "users")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String username;
//    private String email;
//    private String password;
//
//    @Enumerated(EnumType.STRING)
//    private Role role;
//
//    @Builder.Default
//    private boolean enabled = true;
//
//    @Builder.Default
//    private boolean accountNonLocked = true;
//
//    private String otp;
//
//    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @Builder.Default
//    private Set<Recipe> recipes = new HashSet<>();
//    @Column(nullable = false)
//    private Boolean verified = false;
//
//}

package com.recipe.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class    User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean accountNonLocked = true;

    private String otp;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference  // ✅ prevent infinite recursion
    @Builder.Default
    private Set<Recipe> recipes = new HashSet<>();

    @Column(nullable = false)
    private Boolean verified = false;


}
