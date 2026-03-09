    package com.recipe.backend.service;

            import com.recipe.backend.dto.IngredientRequest;
            import com.recipe.backend.dto.IngredientResponse;
            import com.recipe.backend.dto.RecipeRequest;
            import com.recipe.backend.dto.RecipeResponse;
            import com.recipe.backend.model.*;
            import com.recipe.backend.repository.CommentRepository;
            import com.recipe.backend.repository.RecipeRepository;
            import com.recipe.backend.repository.UserRepository;
            import com.recipe.backend.util.RecipeMapper;
            import org.springframework.http.HttpMethod;


            import lombok.RequiredArgsConstructor;
            import org.springframework.beans.factory.annotation.Value;
            import org.springframework.core.ParameterizedTypeReference;
            import org.springframework.data.domain.PageRequest;
            import org.springframework.http.*;
            import org.springframework.security.core.context.SecurityContextHolder;
            import org.springframework.stereotype.Service;
            import org.springframework.transaction.annotation.Transactional;
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.RequestBody;
            import org.springframework.web.client.RestTemplate;
            import org.springframework.web.server.ResponseStatusException;
            import org.springframework.security.access.AccessDeniedException;

            import java.io.*;
            import java.time.LocalDateTime;
            import java.util.*;
            import java.util.concurrent.TimeUnit;
            import java.util.regex.Matcher;
            import java.util.regex.Pattern;
            import java.util.stream.Collectors;



            @Service
                    @RequiredArgsConstructor
                    public class RecipeService {

                private final RecipeRepository recipeRepository;
                private final SpellCheckService spellCheckService;
                private final RecipeMapper recipeMapper;
                private final IngredientService ingredientService;
                private final UserRepository userRepository;
                private final CommentRepository commentRepository;

                private final NutritionService nutritionService;

                        /* =========================
                           CREATE RECIPE
                        ========================== */

//                @PostMapping("/recipes")
//                @Transactional
//                public ResponseEntity<RecipeResponse> createRecipe(@RequestBody RecipeRequest request, User user) {
//                    // 1️⃣ Get the logged-in user
//                    User createdBy = getLoggedInUser();
//
//                    // 2️⃣ Create new Recipe entity
//                    Recipe recipe = new Recipe();
//                    recipe.setTitle(request.getTitle());
//                    recipe.setDescription(request.getDescription());
//                    recipe.setServings(request.getServings() != null ? request.getServings() : 1);
//                    recipe.setCookingTime(request.getCookingTime() != null ? request.getCookingTime() : 0);
//                    recipe.setCreatedBy(createdBy);
//                    recipe.setApproved(null);
//                    recipe.setApproved(createdBy.getRole() == Role.ADMIN);
//                    recipe.setCreatedAt(LocalDateTime.now());
//                    recipe.setUpdatedAt(LocalDateTime.now());
//                    recipe.setIngredients(new HashSet<>());
//
//                    // 3️⃣ Save recipe first to generate ID
//                    Recipe savedRecipe = recipeRepository.save(recipe);
//
//                    // 4️⃣ Initialize totals
//                    double totalCalories = 0.0;
//                    double totalProtein = 0.0;
//                    double totalCarbs = 0.0;
//                    double totalFats = 0.0;
//
//                    // 5️⃣ Loop through ingredients and fetch nutrition
//                    if (request.getIngredients() != null) {
//                        for (IngredientRequest ir : request.getIngredients()) {
//                            if (ir.getName() == null || ir.getName().isBlank()) continue;
//
//                            // Fetch nutrition from your service (USDA / fallback)
//                            Ingredient ing = ingredientService.resolveIngredientNutrition(
//                                    ir.getName(),
//                                    ir.getQuantity() != null ? ir.getQuantity() : 0.0,
//                                    ir.getUnit() != null ? ir.getUnit() : "g",
//                                    recipe
//                            );
//
//                            // Safety: handle nulls
//                            ing.setCalories(Optional.ofNullable(ing.getCalories()).orElse(0.0));
//                            ing.setProtein(Optional.ofNullable(ing.getProtein()).orElse(0.0));
//                            ing.setCarbs(Optional.ofNullable(ing.getCarbs()).orElse(0.0));
//                            ing.setFats(Optional.ofNullable(ing.getFats()).orElse(0.0));
//
//                            // 6️⃣ Link ingredient to saved recipe
//                            ing.setRecipe(savedRecipe);
//                            savedRecipe.getIngredients().add(ing);
//
//                            // 7️⃣ Update totals
//                            totalCalories += ing.getCalories();
//                            totalProtein += ing.getProtein();
//                            totalCarbs += ing.getCarbs();
//                            totalFats += ing.getFats();
//                        }
//                    }
//
//                    // 8️⃣ Update recipe totals
//                    savedRecipe.setTotalCalories(totalCalories);
//                    savedRecipe.setTotalProtein(totalProtein);
//                    savedRecipe.setTotalCarbs(totalCarbs);
//                    savedRecipe.setTotalFats(totalFats);
//
//                    // 9️⃣ Save again to persist ingredients/nutrition
//                    recipeRepository.save(savedRecipe);
//
//                    //  🔟 Build response
//                    RecipeResponse response = RecipeResponse.builder()
//                            .id(savedRecipe.getId())
//                            .title(savedRecipe.getTitle())
//                            .description(savedRecipe.getDescription())
//                            .servings(savedRecipe.getServings())
//                            .cookingTime(savedRecipe.getCookingTime())
//                            .totalCalories(savedRecipe.getTotalCalories())
//                            .totalProtein(savedRecipe.getTotalProtein())
//                            .totalCarbs(savedRecipe.getTotalCarbs())
//                            .totalFats(savedRecipe.getTotalFats())
//                            .approved(recipe.getApproved()) // instead of recipe.isApproved()
//
//                            .createdAt(savedRecipe.getCreatedAt())
//                            .updatedAt(savedRecipe.getUpdatedAt())
//                            .ingredients((Set<IngredientResponse>) savedRecipe.getIngredients().stream()
//                                    .map(ing -> IngredientResponse.builder()
//                                            .id(ing.getId())
//                                            .name(ing.getName())
//                                            .quantity(String.valueOf(ing.getQuantity()))
//                                            .unit(ing.getUnit())
//                                            .calories(ing.getCalories())
//                                            .protein(ing.getProtein())
//                                            .carbs(ing.getCarbs())
//                                            .fats(ing.getFats())
//                                            .build())
//                                    .collect(Collectors.toSet())
//                            )
//                            .build();
//
//                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
//                }

                @Transactional
                public RecipeResponse createRecipe(RecipeRequest request, User user) {
                    // Auto-correct title & description
                    String correctedTitle = spellCheckService.autoCorrect(request.getTitle());
                    String correctedDescription = spellCheckService.autoCorrect(request.getDescription());


                    Recipe recipe = new Recipe();
//                    recipe.setTitle(request.getTitle());
//                    recipe.setDescription(request.getDescription());
                    recipe.setTitle(correctedTitle);
                    recipe.setDescription(correctedDescription);
                    recipe.setServings(request.getServings() != null ? request.getServings() : 1);
                    recipe.setCookingTime(request.getCookingTime() != null ? request.getCookingTime() : 0);
                    recipe.setCreatedBy(user);
                    recipe.setApproved(user.getRole() == Role.ADMIN);
                    recipe.setCreatedAt(LocalDateTime.now());
                    recipe.setUpdatedAt(LocalDateTime.now());
                    recipe.setIngredients(new HashSet<>());

                    Recipe savedRecipe = recipeRepository.save(recipe);

                    double totalCalories = 0.0;
                    double totalProtein = 0.0;
                    double totalCarbs = 0.0;
                    double totalFats = 0.0;

                    if (request.getIngredients() != null) {
                        for (IngredientRequest ir : request.getIngredients()) {
                            if (ir.getName() == null || ir.getName().isBlank()) continue;

                            Ingredient ing = ingredientService.resolveIngredientNutrition(
                                    ir.getName(),
                                    ir.getQuantity() != null ? ir.getQuantity() : 0.0,
                                    ir.getUnit() != null ? ir.getUnit() : "g",
                                    recipe
                            );

                            ing.setCalories(Optional.ofNullable(ing.getCalories()).orElse(0.0));
                            ing.setProtein(Optional.ofNullable(ing.getProtein()).orElse(0.0));
                            ing.setCarbs(Optional.ofNullable(ing.getCarbs()).orElse(0.0));
                            ing.setFats(Optional.ofNullable(ing.getFats()).orElse(0.0));

                            ing.setRecipe(savedRecipe);
                            savedRecipe.getIngredients().add(ing);

                            totalCalories += ing.getCalories();
                            totalProtein += ing.getProtein();
                            totalCarbs += ing.getCarbs();
                            totalFats += ing.getFats();
                        }
                    }

                    savedRecipe.setTotalCalories(totalCalories);
                    savedRecipe.setTotalProtein(totalProtein);
                    savedRecipe.setTotalCarbs(totalCarbs);
                    savedRecipe.setTotalFats(totalFats);

                    recipeRepository.save(savedRecipe);

                    return recipeMapper.toResponse(savedRecipe);
                }
                // =========================
                // GET ONE RECIPE (FIXED)
                //             =========================


                @Transactional(readOnly = true)
                public RecipeResponse getRecipe(Long id) {
                    Recipe recipe = recipeRepository.findById(id)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Recipe with ID " + id + " not found"
                            ));

                    // Ensure ingredient nutrition is not null
                    if (recipe.getIngredients() != null) {
                        for (Ingredient ing : recipe.getIngredients()) {
                            ing.setCalories(ing.getCalories() != null ? ing.getCalories() : 0.0);
                            ing.setProtein(ing.getProtein() != null ? ing.getProtein() : 0.0);
                            ing.setCarbs(ing.getCarbs() != null ? ing.getCarbs() : 0.0);
                            ing.setFats(ing.getFats() != null ? ing.getFats() : 0.0);
                        }
                    }


                    return recipeMapper.toResponse(recipe);
                }


                // =========================
                // GET MY RECIPES (FIXED)
                // =========================
                @Transactional(readOnly = true)
                public List<RecipeResponse> getMyRecipes(User user) {

                    List<Recipe> recipes = (List<Recipe>) recipeRepository.findByCreatedBy(user);

                    return recipes.stream()
                            .map(recipeMapper::toResponse)
                            .toList();
                }

                public Recipe getRecipeById(Long id) {
                    return recipeRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Recipe not found"));
                }

                /* =========================
                   UPDATE RECIPE
                ========================== */
                @Transactional
                public RecipeResponse updateRecipe(Long recipeId, RecipeRequest request, User currentUser) {

                    Recipe recipe = recipeRepository.findById(recipeId)
                            .orElseThrow(() -> new RuntimeException("Recipe not found"));

                    recipe.setTitle(request.getTitle());
                    recipe.setDescription(request.getDescription());
                    recipe.setCookingTime(request.getCookingTime());

                    // 🔥 Clear old ingredients
                    recipe.getIngredients().clear();

                    // 🔥 Add whatever frontend sends (old + new OR fresh)
                    if (request.getIngredients() != null) {
                        for (IngredientRequest ir : request.getIngredients()) {


                            Ingredient ing = new Ingredient();

                            ing.setName(ir.getName().trim());

                            ing.setQuantity(ir.getQuantity() == null ? 100.0 : ir.getQuantity());

                            ing.setUnit(
                                    ir.getUnit() == null || ir.getUnit().trim().isEmpty()
                                            ? "g"
                                            : ir.getUnit().trim()
                            );

                            ing.setCalories(ir.getCalories() == null ? 0 : ir.getCalories());
                            ing.setProtein(ir.getProtein() == null ? 0 : ir.getProtein());
                            ing.setCarbs(ir.getCarbs() == null ? 0 : ir.getCarbs());
                            ing.setFats(ir.getFats() == null ? 0 : ir.getFats());

                            ing.setRecipe(recipe);

                            recipe.getIngredients().add(ing);

                        }
                    }

                    // totals SAFE
                    recipe.setTotalCalories(request.getTotalCalories() == null ? 0.0 : request.getTotalCalories());
                    recipe.setTotalProtein(request.getTotalProtein() == null ? 0.0 : request.getTotalProtein());
                    recipe.setTotalCarbs(request.getTotalCarbs() == null ? 0.0 : request.getTotalCarbs());
                    recipe.setTotalFats(request.getTotalFats() == null ? 0.0 : request.getTotalFats());

                    Recipe saved = recipeRepository.save(recipe);

                    return recipeMapper.toResponse(saved);
                }


                        /* =========================
                           DELETE RECIPE
                        ========================== */

                @Transactional
                public void deleteRecipe(Long recipeId, User requester) {

                    Recipe recipe = getRecipeById(recipeId);
                    User owner = recipe.getCreatedBy();

                    if (requester.getRole() == Role.USER && !owner.getId().equals(requester.getId())) {
                        throw new AccessDeniedException("User cannot delete others' recipes");
                    }

                    if (requester.getRole() == Role.ADMIN &&
                            owner.getRole() == Role.ADMIN &&
                            !owner.getId().equals(requester.getId())) {
                        throw new AccessDeniedException("You can't delete another admin's recipe");
                    }

                    // ✅ Delete all child comments first
                    commentRepository.deleteByRecipeId(recipe.getId()); // see below

                    recipeRepository.delete(recipe);
                }

                /* =========================
                   LIST PAGINATED
                ========================== */
                public List<RecipeResponse> listRecipes(PageRequest pageRequest) {
                    return recipeRepository.findAll(pageRequest)
                            .stream()
                            .map(recipeMapper::toResponse)
                            .toList();
                }

                /* =========================
                   COUNT
                ========================== */
                public long countRecipes() {
                    return recipeRepository.count();
                }


                public List<Recipe> findByUserEmail(String email) {
                    return recipeRepository.findByCreatedBy_Email(email);
                }

                /* =========================
               LIST RECIPES EXCLUDING USER (ADMIN)
            ========================= */
                @Transactional(readOnly = true)
                public List<RecipeResponse> listRecipesUserExcluding(
                        User user,
                        int page,
                        int size
                ) {
                    PageRequest pageRequest = PageRequest.of(page, size);

                    return recipeRepository
                            .findByCreatedByNot(user, pageRequest)
                            .stream()
                            .map(recipeMapper::toResponse)
                            .toList();
                }


                @Transactional(readOnly = true)
                public List<RecipeResponse> getUnapprovedUserRecipes() {
                    return recipeRepository.findByApprovedFalse()  // fetch all unapproved
                            .stream()
                            .filter(r -> r.getCreatedBy().getRole() == Role.USER) // keep only USER
                            .map(recipe -> {
                                RecipeResponse res = recipeMapper.toResponse(recipe);
                                res.setCreatedById(recipe.getCreatedBy().getId());
                                res.setCreatedByUsername(recipe.getCreatedBy().getUsername());
                                res.setCreatedByEmail(recipe.getCreatedBy().getEmail());
                                return res;
                            })
                            .toList();
                }

                @Transactional
                public void approveRecipe(Long recipeId, User admin) {

                    if (admin.getRole() != Role.ADMIN) {
                        throw new RuntimeException("Only admin can approve");
                    }

                    Recipe recipe = recipeRepository.findById(recipeId)
                            .orElseThrow(() -> new RuntimeException("Recipe not found"));

                    recipe.setApproved(true);
                    recipeRepository.save(recipe);


                }


                @Transactional(readOnly = true)
                public List<RecipeResponse> getOtherUsersRecipes(String currentUserEmail) {
                    User currentUser = userRepository.findByEmail(currentUserEmail)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    List<Recipe> recipes = recipeRepository.findApprovedRecipesForOtherUsers(currentUser.getId());

                    return recipes.stream()
                            .map(recipe -> {
                                RecipeResponse res = recipeMapper.toResponse(recipe);
                                res.setCreatedById(recipe.getCreatedBy().getId());
                                res.setCreatedByUsername(recipe.getCreatedBy().getUsername());
                                res.setCreatedByEmail(recipe.getCreatedBy().getEmail());
                                return res;
                            })
                            .toList();
                }

                @Transactional(readOnly = true)
                public List<RecipeResponse> getOtherRecipesForAdmin(String currentEmail) {
                    // Get current logged-in admin
                    User currentAdmin = userRepository.findByEmail(currentEmail)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    // Fetch recipes based on new rules
                    List<Recipe> recipes = recipeRepository.findAllOtherRecipesForAdmin(currentAdmin.getId());

                    // Map to DTO
                    return recipes.stream()
                            .map(recipe -> {
                                RecipeResponse res = recipeMapper.toResponse(recipe);
                                res.setCreatedById(recipe.getCreatedBy().getId());
                                res.setCreatedByUsername(recipe.getCreatedBy().getUsername());
                                res.setCreatedByEmail(recipe.getCreatedBy().getEmail());
                                return res;
                            })
                            .toList();
                }


                // 🔥 ADMIN

                @Transactional(readOnly = true)
                public List<RecipeResponse> getAllRecipes() {
                    return recipeRepository.findAll()
                            .stream()
                            .map(recipeMapper::toResponse)
                            .toList();
                }

                // 🔥 USER

                @Transactional(readOnly = true)
                public List<RecipeResponse> getOtherRecipes(Long userId) {
                    return recipeRepository.findByCreatedBy_IdNot(userId)
                            .stream()
                            .map(recipeMapper::toResponse)
                            .toList();
                }

                public User getLoggedInUser() {
                    String email = SecurityContextHolder.getContext().getAuthentication().getName();
                    return userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                }
                private final String MODEL = "gemma3:1b"; // Ollama model
                // ---------------------------
                    // 1️⃣ Generate INGREDIENTS ONLY
                    // ---------------------------
                    public Set<IngredientResponse> generateIngredients(String recipeName) throws Exception {
                        List<String> lines = runOllama(
                                "List ONLY ingredients for " + recipeName + ". " +
                                        "One ingredient per line. " +
                                        "Format strictly as: IngredientName quantity unit. " +
                                        "Use realistic household cooking quantities. " +
                                        "Grains & vegetables in grams (50–200 g). " +
                                        "Liquids in ml (20–200 ml). " +
                                        "Spices 1–10 g only. " +
                                        "No explanations. No extra text."
                        );


                        Set<IngredientResponse> ingredients = new LinkedHashSet<>();

                        for (String line : lines) {
                            IngredientResponse ingredient = parseIngredient(line);
                            if (ingredient != null) ingredients.add(ingredient);
                        }

                        return ingredients;
                    }

                    // ---------------------------
                    // 2️⃣ Generate DESCRIPTION ONLY
                    // ---------------------------
                    public String generateDescription(String recipeName, List<String> ingredients) throws Exception {

                        // Use only first 5 ingredients to speed up
                        List<String> limitedIngredients = ingredients.size() > 5
                                ? ingredients.subList(0, 5)
                                : ingredients;

                        String prompt = "Write a very short, 1-2 sentence recipe description for '" + recipeName +
                                "' using these ingredients: " + String.join(", ", limitedIngredients) +
                                ". Keep it concise and catchy.";

                        return String.join("\n", runOllama(prompt));
                    }



                    // ---------------------------
                    // 3️⃣ Ingredient PARSER
                    // ---------------------------
//                    private IngredientResponse parseIngredient(String line) {
//                        if (line == null || line.isBlank()) return null;
//
//                        // Remove bullets & markdown
//                        line = line.replaceAll("[•*\\-]+", "").trim();
//
//                        // Extract quantity (number)
//                        String quantity = "100"; // default
//                        String unit = "g";       // default
//
//                        Matcher qtyMatcher = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(line);
//                        if (qtyMatcher.find()) quantity = qtyMatcher.group(1);
//
//                        // Detect unit
//                        if (line.matches(".*(kg|kilogram).*")) unit = "kg";
//                        else if (line.matches(".*(g|gram).*")) unit = "g";
//                        else if (line.matches(".*(ml|milliliter).*")) unit = "ml";
//                        else if (line.matches(".*(l|liter).*")) unit = "l";
//                        else if (line.matches(".*(tsp|teaspoon).*")) unit = "tsp";
//                        else if (line.matches(".*(tbsp|tablespoon).*")) unit = "tbsp";
//                        else if (line.matches(".*(cup).*")) unit = "cup";
//
//                        // Clean ingredient name
//                        String name = line
//                                .replaceAll("(\\d+(\\.\\d+)?)", "")
//                                .replaceAll("(kg|kilogram|g|gram|ml|milliliter|l|liter|tsp|teaspoon|tbsp|tablespoon|cup)", "")
//                                .replaceAll("to taste|as required", "")
//                                .trim();
//
//                        if (name.isBlank()) return null;
//
//                        return IngredientResponse.builder()
//                                .name(name)
//                                .quantity(Double.valueOf(quantity))
//                                .unit(unit)
//                                .build();
//                    }


                private IngredientResponse parseIngredient(String line) {
                    if (line == null || line.isBlank()) return null;

                    // 1️⃣ Remove bullets & markdown
                    line = line.replaceAll("[•*\\-]+", "").trim();

                    // 2️⃣ Extract quantity (number)
                    String quantity = "100"; // default
                    String unit = "g";       // default

                    Matcher qtyMatcher = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(line);
                    if (qtyMatcher.find()) quantity = qtyMatcher.group(1);

                    // 3️⃣ Detect unit
                    if (line.matches(".*(kg|kilogram).*")) unit = "kg";
                    else if (line.matches(".*(g|gram).*")) unit = "g";
                    else if (line.matches(".*(ml|milliliter).*")) unit = "ml";
                    else if (line.matches(".*(l|liter).*")) unit = "l";
                    else if (line.matches(".*(tsp|teaspoon).*")) unit = "tsp";
                    else if (line.matches(".*(tbsp|tablespoon).*")) unit = "tbsp";
                    else if (line.matches(".*(cup).*")) unit = "cup";

                    // 4️⃣ Clean ingredient name
                    String name = line
                            .replaceAll("(\\d+(\\.\\d+)?)", "")
                            .replaceAll("(kg|kilogram|g|gram|ml|milliliter|l|liter|tsp|teaspoon|tbsp|tablespoon|cup)", "")
                            .replaceAll("to taste|as required", "")
                            .trim();

                    // 5️⃣ Apply spell-check
                    if (!name.isBlank()) {
                        name = spellCheckService.autoCorrect(name);
                    }

                    if (name.isBlank()) return null;

                    // 6️⃣ Build response
                    return IngredientResponse.builder()
                            .name(name)
                            .quantity(Double.valueOf(quantity))
                            .unit(unit)
                            .build();
                }


                // ---------------------------
                    // 4️⃣ Ollama CALL
                    // ---------------------------
//                    private List<String> runOllama(String prompt) throws Exception {
//                        ProcessBuilder pb = new ProcessBuilder(
//                                "ollama",
//                                "run",
//                                MODEL
//                        );
//                        pb.redirectErrorStream(true);
//                        Process process = pb.start();
//
//                        // Send prompt
//                        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
//                            writer.write(prompt);
//                            writer.newLine();
//                            writer.flush();
//                        }
//
//                        // Read output
//                        List<String> output = new ArrayList<>();
//                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                            String line;
//                            while ((line = reader.readLine()) != null) {
//                                line = stripAnsi(line);
//                                if (!line.isBlank()) output.add(line.trim());
//                            }
//                        }
//
//                        // Timeout after 60s
//                        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
//                        if (!finished) {
//                            process.destroyForcibly();
//                            throw new RuntimeException("AI generation timeout");
//                        }
//
//                        return output;
//                    }
                    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
                private final RestTemplate restTemplate = new RestTemplate();

                private List<String> runOllama(String prompt) {

                    Map<String, Object> body = new HashMap<>();
                    body.put("model", MODEL);
                    body.put("prompt", prompt);
                    body.put("stream", false);   // 🔥 VERY IMPORTANT
                    body.put("options", Map.of(
                            "temperature", 0.2,
                            "num_predict", 120
                    ));

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<Map<String, Object>> request =
                            new HttpEntity<>(body, headers);

                    ResponseEntity<Map> response =
                            restTemplate.postForEntity(OLLAMA_URL, request, Map.class);

                    String output = (String) response.getBody().get("response");

                    return Arrays.stream(output.split("\\r?\\n"))
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .toList();
                }


                // ---------------------------
                    // 5️⃣ Clean ANSI from Ollama output
                    // ---------------------------
                    private String stripAnsi(String input) {
                        return input
                                // Remove ANSI escape sequences
                                .replaceAll("\\u001B\\[[0-9;?]*[a-zA-Z]", "")
                                // Remove spinner & control chars
                                .replaceAll("[^\\x20-\\x7E]", "")
                                .trim();
                    }

            }












