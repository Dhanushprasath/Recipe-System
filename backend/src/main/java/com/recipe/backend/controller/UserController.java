package com.recipe.backend.controller;

    import com.recipe.backend.dto.RecipeRequest;
    import com.recipe.backend.dto.RecipeResponse;
    import com.recipe.backend.dto.UserDto;
    import com.recipe.backend.model.Recipe;
    import com.recipe.backend.model.User;
    import com.recipe.backend.repository.UserRepository;
    import com.recipe.backend.service.RecipeService;
    import com.recipe.backend.service.UserService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.Map;
    import java.util.NoSuchElementException;

//    @RestController
//    @RequestMapping("/api/users")
//    @RequiredArgsConstructor
//    public class UserController {
//
//        private final UserRepository userRepository;
//        private final RecipeService recipeService;
//        private final UserService userService;
//
//
//        // =========================
//        // GET CURRENT USER INFO
//        // =========================
//        @GetMapping("/me")
//        public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
//            if (userDetails == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
//            }
//
//            var userOpt = userRepository.findByEmail(userDetails.getUsername());
//
//            if (userOpt.isPresent()) {
//                User user = userOpt.get();
//                UserDto dto = new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
//                return ResponseEntity.ok(dto);
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
//            }
//        }
//
//        // =========================
//        // GET MY RECIPES
//        // =========================
//        @GetMapping("/my-recipes")
//        public ResponseEntity<List<RecipeResponse>> getMyRecipes(Authentication authentication) {
//            String email = authentication.getName();
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            List<RecipeResponse> recipes = recipeService.getMyRecipes(user);
//            return ResponseEntity.ok(recipes);
//        }
//
//        // =========================
//        // CREATE RECIPE
//        // =========================
//        @PostMapping("/recipes")
//        public ResponseEntity<RecipeResponse> createRecipe(
//                @RequestBody RecipeRequest request,
//                Authentication authentication
//        ) {
//            String email = authentication.getName();
//            User user = userRepository.findByEmail(authentication.getName())
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            RecipeResponse created = recipeService.createRecipe(request, user);
//            return ResponseEntity.status(HttpStatus.CREATED).body(created);
//        }
//
//        // =========================
//        // GET ONE RECIPE
//        // =========================
//        @GetMapping("/recipes/{id}")
//        public ResponseEntity<?> getRecipe(@PathVariable Long id) {
//            try {
//                RecipeResponse recipe = recipeService.getRecipe(id);
//                return ResponseEntity.ok(recipe);
//            } catch (NoSuchElementException e) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(Map.of("message", "Recipe not found"));
//            }
//        }
//
//        // =========================
//        // UPDATE RECIPE (USER CAN ONLY UPDATE OWN)
//        // =========================
//        @PutMapping("/recipes/{id}")
//        public ResponseEntity<?> updateRecipe(
//                @PathVariable Long id,
//                @RequestBody RecipeRequest request,
//                Authentication authentication
//        ) {
//            try {
//                User user = userRepository.findByEmail(authentication.getName())
//                        .orElseThrow(() -> new RuntimeException("User not found"));
//
//                RecipeResponse updated = recipeService.updateRecipe(id, request,user );
//
//                // Check ownership
//                Recipe recipeEntity = recipeService.getRecipeById(id);
//                if (!recipeEntity.getCreatedBy().getId().equals(user.getId())) {
//                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                            .body(Map.of("error", "You cannot update others' recipes"));
//                }
//
//                return ResponseEntity.ok(updated);
//            } catch (NoSuchElementException e) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(Map.of("message", "Recipe not found"));
//            } catch (Exception e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(Map.of("error", e.getMessage()));
//            }
//        }
//
//        // =========================
//        // DELETE RECIPE (USER CAN ONLY DELETE OWN)
//        // =========================
//        @DeleteMapping("/recipes/{id}")
//        public ResponseEntity<?> deleteRecipe(
//                @PathVariable Long id,
//                Authentication authentication
//        ) {
//            try {
//                User user = userRepository.findByEmail(authentication.getName())
//                        .orElseThrow(() -> new RuntimeException("User not found"));
//
//                Recipe recipe = recipeService.getRecipeById(id);
//
//                if (!recipe.getCreatedBy().getId().equals(user.getId())) {
//                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                            .body(Map.of("error", "You cannot delete others' recipes"));
//                }
//
//                recipeService.deleteRecipe(id, user);
//                return ResponseEntity.ok(Map.of("message", "Recipe deleted successfully"));
//
//            } catch (RuntimeException e) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(Map.of("error", e.getMessage()));
//            }
//        }
//
//        // =========================
//        // GET OTHER USERS' RECIPES
//        // =========================
//        @GetMapping("/recipes/others")
//        public ResponseEntity<List<RecipeResponse>> getOtherUsersRecipes(
//                Authentication authentication,
//                @RequestParam(defaultValue = "0") int page,
//                @RequestParam(defaultValue = "10") int size
//        ) {
//            User user = userRepository.findByEmail(authentication.getName())
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            List<RecipeResponse> allRecipes =
//                    recipeService.listRecipesUserExcluding(user, page, size);
//
//            return ResponseEntity.ok(allRecipes);
//        }
//        @GetMapping("/other")
//        public List<RecipeResponse> getOtherRecipes(Authentication auth) {
//            User user = userService.getCurrentUser(auth);
//            return recipeService.getOtherRecipes(user.getId());
//        }
//        @PutMapping("/update-username")
//        public ResponseEntity<?> updateUsername(
//                @RequestBody Map<String, String> request,
//                Authentication authentication) {
//
//            String username = request.get("username");
//
//            if (username == null || username.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body("Username is required");
//            }
//
//            userService.updateUsername(authentication.getName(), username.trim());
//
//            return ResponseEntity.ok().build();
//        }
//
//    }
//
//
//
//
//
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    // =========================
    // GET CURRENT USER INFO
    // =========================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDto dto = new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(dto);
    }

    // =========================
    // UPDATE USERNAME
    // =========================
    @PutMapping("/update-username")
    public ResponseEntity<?> updateUsername(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String username = request.get("username");

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required");
        }

        userService.updateUsername(authentication.getName(), username.trim());
        return ResponseEntity.ok().build();
    }
}


