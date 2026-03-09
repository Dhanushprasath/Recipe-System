console.log("dashboard.js   ed");
const API_BASE = "http://localhost:8081/api";
const token = localStorage.getItem("token");
let recognition = null;
let activeVoiceField = null;
let dashboardcontent;
let dashboardMenu;
let userEmail = null; 
let currentViewedRecipeId = null;
let isFavoriteRecipe = false;   
function authHeader() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
    };
}
document.addEventListener("DOMContentLoaded", async() => {
    dashboardContent = document.getElementById("dashboardcontent");
    dashboardMenu = document.getElementById("dashboardMenu");
    if (!dashboardContent) {
        console.error("dashboardcontent not found");
        return;
    }
        if (!token) {
        alert("Please login again");
        window.location.href = "login.html";
        return;
    }await loadCurrentUser();
    document.getElementById("btn-my-recipes")?.addEventListener("click", loadMyRecipes);
    document.getElementById("btn-add-recipe")?.addEventListener("click", toggleAddRecipeForm);
    document.getElementById("btn-other-recipes")?.addEventListener("click", loadOtherRecipes);
    document.getElementById("btn-favorites")?.addEventListener("click", loadFavorites);

    document.getElementById("btn-dietlog")?.addEventListener("click", initDietLogMenu);
    document.getElementById("btn-profile")?.addEventListener("click", loadProfile);
    document.getElementById("btn-logout")?.addEventListener("click", logout);
    dashboardContent.innerHTML = "<p>Select an option</p>";
});
function initDietLogMenu() {
    dashboardMenu.innerHTML = `
        <button onclick="fetchAllLogs()">All Logs</button>
        <button onclick="renderAddDietLogForm()">Add / Update Log</button>
        <button onclick="fetchWeeklyLogs()">Weekly Logs</button>
        <button onclick="fetchMonthlyLogs()">Monthly Logs</button>
    `;
    fetchAllLogs(); // default view
}

/* ===============================
   OTHER USERS' RECIPES
================================ */
async function loadOtherRecipes() {
    dashboardMenu.innerHTML = "";
    dashboardContent.innerHTML = `
        <h2>Other Users Recipes</h2>
        <div id="recipesContainer"></div>
    `;
    const container = document.getElementById("recipesContainer");
    if (!container) {
        console.error("recipesContainer element not found");
        return;
    }
    try {
        const res = await fetch(
            `${API_BASE}/recipes/others?currentUserEmail=${encodeURIComponent(userEmail)}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        const text = await res.text();
        let recipes;
        try {
            recipes = JSON.parse(text);
        } catch (e) {
            console.error("Invalid JSON:", text);
            container.innerHTML = "<p>Backend error</p>";
            return;
        }
        if (recipes.length === 0) {
            container.innerHTML = "<p>No recipes found</p>";
            return;
        }
        recipes.forEach(recipe => {
            container.innerHTML += `
                <div class="recipe-card">
                    <h3>${recipe.title}</h3>
                    <p><b>Created by:</b> ${recipe.createdByUsername}</p>

                  
                   <button onclick="viewRecipe(${recipe.id})" 
    style="background-color: #4CAF50; color: white; border: none; padding: 8px 15px; border-radius: 5px; cursor: pointer;">
    View
</button>
     <button class="btn comment" onclick="toggleComments(${recipe.id})">Comments</button>
     <div class="comment-section" id="commentSection-${recipe.id}" style="display:none;">
    <div id="commentsList-${recipe.id}"></div>

    <textarea id="commentInput-${recipe.id}" placeholder="Write a comment"></textarea>
 <button 
  onclick="postComment(${recipe.id})"
  style="background-color:#4CAF50;color:white;border:none;padding:6px 14px;border-radius:5px;cursor:pointer;">
  Post
</button>

</div>


     
                </div>
            `;
        });

    } catch (err) {
        console.error(err);
        container.innerHTML = "<p>Error loading recipes</p>";
    }
}
async function toggleFavoriteRecipe() {
    if (!checkToken() || !currentViewedRecipeId) return;

    try {
        if (isFavoriteRecipe) {
            await fetch(`${API_BASE}/recipes/favorite/${currentViewedRecipeId}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` }
            });
            isFavoriteRecipe = false;
            alert("💔 Removed from favorites");
        } else {
            await fetch(`${API_BASE}/recipes/favorite/${currentViewedRecipeId}`, {
                method: "POST",
                headers: { Authorization: `Bearer ${token}` }
            });
            isFavoriteRecipe = true;
            alert("❤️ Added to favorites");
        }

        updateFavoriteButton();

    } catch (err) {
        console.error(err);
        alert("Failed to toggle favorite");
    }
}



function addToFavorite(recipeId) {
    fetch(`${API_BASE}/recipes/favorite/${recipeId}`, {

        method: "POST",
        headers: {
            Authorization: "Bearer " + token
        }
    })
    .then(res => {
        if (!res.ok) throw new Error("Failed to add favorite");
        alert("❤️ Added to favorites");
    })
    .catch(err => alert(err.message));
}
function checkToken() {
    if (!token) {
        dashboardContent.innerHTML = "<p style='color:red'>Please login again</p>";
        return false;
    }
    return true;
}
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("email");
    window.location.href = "login.html";
}
//================
// loadcurrentuser
//================
async function loadCurrentUser() {
    dashboardMenu.innerHTML = "";
    if (!token) return;

    try {
        const res = await fetch(`${API_BASE}/users/me`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("User fetch failed");

        const user = await res.json();

        const usernameEl = document.getElementById("username");
if (usernameEl) {
    usernameEl.innerText = user.username;
}


        userEmail = user.email;
        localStorage.setItem("email", user.email); // ✅ REQUIRED
        localStorage.setItem("userId", user.id);
        localStorage.setItem("role", user.role);
                return user; 

    } catch (err) {
        console.error(err);
    }
}
/* ===============================
   MY RECIPES
================================ */
    async function loadMyRecipes() {
        dashboardMenu.innerHTML = "";
        if (!checkToken()) return;

        const email = userEmail || localStorage.getItem("email");

if (!email) {
    dashboardContent.innerHTML =
        "<p style='color:red'>Session expired. Please login again.</p>";
    return;
}


    
        dashboardContent.innerHTML = `
        <h2>My Recipes</h2>
        <p id="loadingText">Loading...</p>
        <div id="recipeContainer"></div>
    `;


        try {
            const res = await fetch(
           `${API_BASE}/recipes/my`, 
            {
                headers: { Authorization: `Bearer ${token}` }
            }
        );          

            if (!res.ok) throw new Error("Failed to load my recipes");

            const recipes = await res.json();
            displayRecipes(recipes);
            document.getElementById("loadingText")?.remove();

        } catch (err) {
                document.getElementById("loadingText")?.remove();
                dashboardContent.innerHTML += `<p style="color:red">${err.message}</p>`;
        }
    }

    //=================
    // display recipe
    //====================
    function displayRecipes(recipes) {
    if (!recipes || recipes.length === 0) {
        dashboardContent.innerHTML = "<p>No recipes found.</p>";
        return;
    }

    let html = `<div class="recipe-list">`;

    recipes.forEach(recipe => {
        let status = "";
        if (recipe.approved) status = "<span class='status approved'>APPROVED</span>";
        else if (recipe.rejected) status = "<span class='status rejected'>REJECTED</span>";
        else status = "<span class='status pending'>PENDING</span>";

        html += `
            <div class="recipe-card" id="recipeCard-${recipe.id}">
                <h3 class="recipe-title">${recipe.title}</h3>
                <p class="recipe-status">Status: ${status}</p>
                <p class="recipe-date">📅 ${formatDateTime(recipe.createdAt)}</p>
                <div class="recipe-actions">
                    <button class="btn view" onclick="viewRecipe(${recipe.id})">View</button>
                    <button class="btn edit" onclick="editRecipe(${recipe.id})">Edit</button>
                    <button class="btn delete" onclick="deleteRecipe(${recipe.id})">Delete</button>
                    <button class="btn comment" onclick="toggleComments(${recipe.id})">Comments</button>
                </div>
                 <div class="comment-section"
                     id="commentSection-${recipe.id}"
                     style="display:none;">

                    <div id="commentsList-${recipe.id}"></div>

                    <textarea
                        id="commentInput-${recipe.id}"
                        placeholder="Write a comment">
                    </textarea>

                    <button class="post-btn"
                        onclick="postComment(${recipe.id})">
                        Post
                    </button>
                </div>      
            </div>
        `;
    });

    html += `</div>`;
    document.getElementById("recipeContainer").innerHTML = html;
}


//==================
// add recipe
//==================
function toggleAddRecipeForm() {
     dashboardMenu.innerHTML = "";
    if (!checkToken()) return;

    dashboardContent.innerHTML = `
        <h2>Add New Recipe</h2>
        <form id="addRecipeForm" style="max-width:600px;">
            <label>Name</label>
            <input type="text" id="recipeName" required>

            <label>Ingredients</label>
            <textarea id="ingredients"></textarea>
             <button type="button" class="btn btn-green" onclick="startVoice('ingredients')">🎤 Ingredients Voice</button>
           
            <label>Description</label>
            <textarea id="description"></textarea>
           <button type="button" class="btn btn-green" onclick="startVoice('description')">
           🎤 Description Voice</button>

            
            <label>Nutrition</label>
            <textarea id="nutrition" readonly></textarea>

            <button type="button" class="btn btn-green" onclick="fetchNutrition()">Fetch Nutrition</button>
            <br><br>
            
    <div class="form-group">
    <label for="cookingTime">Cooking Time (minutes)</label>
    <input 
        type="number" 
        id="cookingTime" 
        name="cookingTime"  
        min="1"
        required
        class="form-control"
    >
</div>
  
<button type="submit" id="saveBtn" class="btn btn-green">Save</button>

        </form>
    `;

    document.getElementById("addRecipeForm").addEventListener("submit", async (e) => {
        e.preventDefault();
        await saveRecipe();
    });
}
// ---------- PARSE INGREDIENTS ----------
function parseIngredients(ingredientsText) {
    try {
        return ingredientsText.split("\n").map(line => {
            const parts = line.trim().split(" ");
            if (parts.length < 3) {
                throw new Error("Each ingredient must have: name quantity unit");
            }

            const quantity = Number(parts[1]);
            if (isNaN(quantity)) {
                throw new Error("Quantity must be a number for " + parts[0]);
            }

            return {
                name: parts[0],
                quantity: quantity,
                unit: parts[2]
            };
        });
    } catch (err) {
        alert("❌ Ingredient parsing error: " + err.message);
        console.error(err);
        return null; // stop further execution
    }
}

// ---------- SAVE RECIPE ----------
function extractValue(text,label) {
    const regex = new RegExp(label + ":\\s*([\\d.]+)", "i");
    const match = text.match(regex);
    return match ? Number(match[1]) : 0;
}



async function saveRecipe() {
    const ingredientsText = document.getElementById("ingredients").value.trim();
    const ingredientArray = parseIngredients(ingredientsText);
    if (!ingredientArray) return;

    const cookingTime = Number(document.getElementById("cookingTime").value);
    if (!cookingTime || cookingTime <= 0) {
        alert("Cooking time must be greater than 0");
        return;
    }

    const recipeData = {
        title: document.getElementById("recipeName").value.trim(),
        description: document.getElementById("description").value.trim(),
        ingredients: ingredientArray,
        cookingTime: cookingTime
    };

    try {
        // STEP 1: Save the recipe
        const res = await fetch(`${API_BASE}/recipes`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify(recipeData)
        });

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err);
        }

        const createdRecipe = await res.json();
        const recipeId = createdRecipe.id;

        // STEP 2: Save nutrition AFTER recipe is created
        const nutritionText = document.getElementById("nutrition").value;
        const nutritionData = [{
            calories: extractValue("Calories", nutritionText),
            protein: extractValue("Protein", nutritionText),
            carbs: extractValue("Carbs", nutritionText),
            fats: extractValue("Fat", nutritionText),
            fiber: 0,
            sodium: 0
        }];

        await fetch(`${API_BASE}/nutrition/save/${recipeId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify({ nutrition: nutritionData }) // ✅ wrapped in object
        });

        alert("✅ Recipe and nutrition saved successfully!");
        loadMyRecipes();

    } catch (err) {
        console.error(err);
        alert("❌ " + err.message);
    }
}

function startVoice(fieldId) {
    if (!("webkitSpeechRecognition" in window)) {
        alert("Voice input not supported in this browser");
        return;
    }

    // Stop previous recognition if running
    if (recognition) {
        recognition.stop();
        recognition = null;
    }

    activeVoiceField = fieldId;

    recognition = new webkitSpeechRecognition();
    recognition.lang = "en-US";
    recognition.continuous = false;
    recognition.interimResults = false;

    recognition.start();

    recognition.onresult = (event) => {
        const spokenText = event.results[0][0].transcript.trim();
        const field = document.getElementById(activeVoiceField);

        if (field) {
            field.value += (field.value ? " " : "") + spokenText;
        }
    };

    recognition.onerror = (event) => {
        console.error("Voice error:", event.error);
        alert("Voice recognition failed");
    };

    recognition.onend = () => {
        recognition = null;
        activeVoiceField = null;
    };
}


function extractCalories(text) {
    if (!text) return 0;
    const match = text.match(/Calories:\s*(\d+)/i);
    return match ? Number(match[1]) : 0;
}


function formatDateTime(dateTime) {
    if (!dateTime) return "-";
    const d = new Date(dateTime);
    return d.toLocaleString(); // shows date + time
}
/* ===============================
   viewrecipe
================================ */
async function viewRecipe(id) {
    dashboardMenu.innerHTML = "";
    if (!checkToken()) return;

    dashboardContent.innerHTML = "<p>Loading recipe...</p>";

    try {
        const res = await fetch(`${API_BASE}/recipes/${id}`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) {
            let errorData;
            try { errorData = await res.json(); } 
            catch { errorData = { message: `HTTP ${res.status}` }; }
            throw new Error(errorData.message || "Failed to load recipe");
        }

        const text = await res.text();
        if (!text) throw new Error("Recipe data is empty");
        const recipe = JSON.parse(text);

        renderViewRecipeForm(recipe);

    } catch (err) {
        dashboardContent.innerHTML = `<p style="color:red">${err.message}</p>`;
    }
}
/* ===============================
   renderviewrecipe
================================ */
function renderViewRecipeForm(recipe) {
    currentViewedRecipeId = recipe.id;
    let ingredientsHtml = "No ingredients";

    if (Array.isArray(recipe.ingredients) && recipe.ingredients.length > 0) {
        ingredientsHtml = recipe.ingredients
            .map(i => {
                if (typeof i === "string") return i; // fallback if backend returns strings
                return `${i.name} - ${i.quantity ?? ""} ${i.unit ?? ""}`.trim();
            })
            .join("\n");
    }

    dashboardContent.innerHTML = `
        <h2>View Recipe</h2>

        <form style="max-width:600px;">
            <label>Recipe Name</label>
            <input type="text" value="${recipe.title || ""}" readonly>

            <label>Description</label>
            <textarea readonly>${recipe.description || ""}</textarea>

            <label>Ingredients</label>
            <textarea readonly rows="5">${ingredientsHtml}</textarea>

            <label>Nutrition</label>
            <textarea readonly rows="4">
Calories: ${recipe.totalCalories ?? 0} kcal
Protein: ${recipe.totalProtein ?? 0} g
Carbs: ${recipe.totalCarbs ?? 0} g
Fats: ${recipe.totalFats ?? 0} g
            </textarea>

            <div class="form-group">
                <label>Cooking Time (minutes)</label>
                <input type="number" value="${recipe.cookingTime ?? 0}" readonly>
            </div>
                      <!-- ✅ FAVORITE BUTTON -->
            <button type="button" id="favoriteBtn" class="btn btn-green" onclick="toggleFavoriteRecipe()">🤍 Add to Favorites</button><br><br>
            <button type="button" class="btn" onclick="loadMyRecipes()">⬅ Back</button>
        </form>
    `;
    const loggedInEmail = localStorage.getItem("email");
    if (recipe.createdBy?.email === loggedInEmail) {
        document.getElementById("favoriteBtn").style.display = "none";
    }

    checkIfFavorite(recipe.id);
}
//===================
// fetchnutrition
//====================

async function fetchNutrition() {
    const ingredientsField = document.getElementById("ingredients");
    const nutritionBox = document.getElementById("nutrition");

    const rawText = ingredientsField.value;
    if (!rawText || !rawText.trim()) {
        alert("Enter ingredients");
        return;
    }
const ingredients = rawText
.split("\n")
.map(line => {

    line = line.replace("null","").trim();

    // normalize: convert multiple spaces / hyphens to single space
    line = line.replace(/[-]+/g, " ").replace(/\s+/g, " ");

    // match: name quantity unit
    const match = line.match(/^(.+?)\s+([\d.]+)\s*(\w+)$/);

    if (!match) return null;

    return {
        name: match[1].trim(),
        quantity: parseFloat(match[2]) || 100,
        unit: match[3] || "g"
    };
})
.filter(Boolean);


    if (ingredients.length === 0) {
        alert("No valid ingredients found");
        return;
    }

    nutritionBox.value = "Fetching nutrition...";

    try {
        // Try USDA API first
        const res = await fetch(`${API_BASE}/nutrition/fetch`, {

            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ ingredients })
        });

        if (!res.ok) throw new Error("USDA API failed");

        const data = await res.json();
        nutritionBox.value = 
`Calories: ${Math.round(data.calories)} kcal
Protein: ${data.protein.toFixed(1)} g
Carbs: ${data.carbs.toFixed(1)} g
Fat: ${data.fat.toFixed(1)} g`;

    } catch (err) {
        console.warn("USDA API failed, fetching from backend", err);

        // ✅ Fallback to your backend
        try {
            const fallbackRes = await fetch(`${API_BASE}/nutrition/fallback`, {

                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ ingredients })
            });

            if (!fallbackRes.ok) throw new Error("Backend fallback failed");

            const data = await fallbackRes.json();
            nutritionBox.value = 
`Calories: ${Math.round(data.calories)} kcal
Protein: ${data.protein.toFixed(1)} g
Carbs: ${data.carbs.toFixed(1)} g
Fat: ${data.fat.toFixed(1)} g`;

        } catch (backendErr) {
            console.error("Backend fallback failed", backendErr);
            nutritionBox.value = "Error fetching nutrition";
        }
    }
}


// ===============================
// Edit Recipe
// ===============================
async function editRecipe(id) {
    if (!checkToken()) return;

    dashboardContent.innerHTML = "<p>Loading recipe...</p>";

    try {
        const res = await fetch(`${API_BASE}/recipes/${id}`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to load recipe");

        const recipe = await res.json();

        // Convert ingredient objects to textarea-friendly string: "name-quantity-unit"
        const ingredientsText = recipe.ingredients
                .map(i => {
                const quantity = i.quantity ?? "";
                const unit = i.unit ?? "g";
                return `${i.name} ${quantity} ${unit}`.trim(); // NO extra hyphen!
            })

           .join("\n");

        dashboardContent.innerHTML = `
            <h2>Edit Recipe</h2>
            <form id="editRecipeForm">
                <label>Title</label>
                <input type="text" id="title" value="${recipe.title}" required>

                <label>Ingredients (name-quantity-unit per line)</label>
                <textarea id="ingredients" rows="5">${ingredientsText}</textarea>
                <button type="button" class="btn btn-green" onclick="startVoice('ingredients')">🎤 Ingredients Voice</button>
               
                <label>Description</label>
                <textarea id="description">${recipe.description || ""}</textarea>
                <button type="button" class="btn btn-green" onclick="startVoice('description')">🎤 Description Voice</button>

                <label>Nutrition</label>
               
                
                <textarea id="nutrition" readonly>
Calories: ${recipe.totalCalories ?? 0} kcal
Protein: ${recipe.totalProtein ?? 0} g
Carbs: ${recipe.totalCarbs ?? 0} g
Fat: ${recipe.totalFats ?? 0} g
</textarea>
                <button type="button" class="btn btn-green" onclick="fetchNutrition()">Fetch Nutrition</button>

                <label>Cooking Time (minutes)</label>
                <input type="number" id="cookingTime" value="${recipe.cookingTime}" min="1" required>

                <br><br>
                <button type="submit" class="btn btn-green">Update</button>
                <button type="button" class="btn" onclick="loadMyRecipes()">Cancel</button>
            </form>
        `;

        document.getElementById("editRecipeForm").addEventListener("submit", async (e) => {
            e.preventDefault();
            await updateRecipe(id);
        });

    } catch (err) {
        dashboardContent.innerHTML = `<p style="color:red">${err.message}</p>`;
    }
}

//--------------------
//  update recipe
//-------------------
// async function updateRecipe(id) {
//     const ingredientsString = document.getElementById("ingredients").value.trim();

//     // Convert to array of {name, quantity, unit} objects
//     const ingredientArray = ingredientsString
//         .split("\n")
//         .map(line => {
//             const parts = line.split("-").map(p => p.trim());
//             return {
//                 name: parts[0] || "",
//                 quantity: parts[1] || "",
//                 unit: parts[2] || ""
//             };
//         })
//         .filter(i => i.name);

//     const recipeData = {
//         title: document.getElementById("title").value.trim(),
//         ingredients: ingredientArray,
//         description: document.getElementById("description").value.trim(),
//         nutrition: [{
//     calories: extractCalories(document.getElementById("nutrition").value),
//     protein: 0,
//     carbs: 0,
//     fats: 0,
//     fiber: 0,
//     sodium: 0
// }],

//         totalCalories: extractCalories(document.getElementById("nutrition").value),
//         cookingTime: Number(document.getElementById("cookingTime").value)
//     };

//     if (!recipeData.title || recipeData.cookingTime <= 0) {
//         alert("Please fill all required fields");
//         return;
//     }

//     try {
//         const res = await fetch(`${API_BASE}/recipes/${id}`, {
//             method: "PUT",
//             headers: {
//                 "Content-Type": "application/json",
//                 Authorization: `Bearer ${token}`
//             },
//             body: JSON.stringify(recipeData)
//         });

//         if (!res.ok) {
//             const errorText = await res.text();
//             throw new Error(errorText || "Update failed");
//         }

//         alert("✅ Recipe updated successfully!");
//         loadMyRecipes();
//     } catch (err) {
//         console.error("Update error:", err);
//         alert("❌ " + err.message);
//     }
// }


async function updateRecipe(id) {

const ingredientsString = document.getElementById("ingredients").value.trim();
const nutritionText = document.getElementById("nutrition").value;

const ingredientArray = ingredientsString
        .split("\n")
        .map(line => {
            line = line.trim();
            if (!line) return null;

            let name = "", quantity = "", unit = "";

            if (line.includes("-")) {
                // hyphen format: "rice - 100 - g"
                const parts = line.split("-").map(p => p.trim());
                name = parts[0] || "";
                quantity = parts[1] || "";
                unit = parts[2] || "g";
            } else {
                // space format: "rice 100 g"
                const match = line.match(/^(.+?)\s+([\d.]+)\s*(\w+)$/);
                if (match) {
                    name = match[1];
                    quantity = match[2];
                    unit = match[3];
                } else {
                    name = line; // fallback
                    quantity = "";
                    unit = "g";
                }
            }

            return {
                name,
                quantity,
                unit,
                calories: extractValue(nutritionText,"Calories"),
                protein: extractValue(nutritionText,"Protein"),
                carbs: extractValue(nutritionText,"Carbs"),
                fats: extractValue(nutritionText,"Fat")
            };
        })
        .filter(i => i && i.name);

const recipeData = {
    title: document.getElementById("title").value.trim(),
    description: document.getElementById("description").value.trim(),
    cookingTime: Number(document.getElementById("cookingTime").value),

    ingredients: ingredientArray,

    // 🔥 totals
    totalCalories: extractValue(nutritionText,"Calories"),
    totalProtein: extractValue(nutritionText,"Protein"),
    totalCarbs: extractValue(nutritionText,"Carbs"),
    totalFats: extractValue(nutritionText,"Fat")
};

try {
    const res = await fetch(`${API_BASE}/recipes/${id}`, {
        method:"PUT",
        headers:{
            "Content-Type":"application/json",
            Authorization:`Bearer ${token}`
        },
        body: JSON.stringify(recipeData)
    });

    if(!res.ok) throw await res.text();

    alert("Recipe updated!");
    loadMyRecipes();

} catch(e){
    alert(e);
}
}

//----------------------------------
// DELETE RECIPE  
//----------------------------------
async function deleteRecipe(recipeId) {
    const token = localStorage.getItem("token");

    if (!recipeId) {
        alert("❌ Recipe ID missing");
        return;
    }
    if (!token) {
        alert("Session expired. Please login again.");
        return;
    }

    if (!confirm("Are you sure you want to delete this recipe?")) return;

    try {
        const res = await fetch(`${API_BASE}/recipes/${recipeId}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || "Delete failed");
        }

        alert("✅ Recipe deleted successfully!");

        // Remove recipe card instantly
        const card = document.getElementById(`recipeCard-${recipeId}`);
        if (card) card.remove();

    } catch (err) {
        console.error(err);
        alert("❌ " + err.message);
    }
}
function removeFromFavorite(recipeId) {
    fetch(`${API_BASE}/recipes/favorite/${recipeId}`, {

        method: "DELETE",
        headers: {
            Authorization: "Bearer " + token
        }
    }).then(() => alert("💔 Removed from favorites"));
}
async function checkIfFavorite(recipeId) {
    try {
        const res = await fetch(`${API_BASE}/recipes/favorites`, {

            headers: { Authorization: "Bearer " + token }
        });

        if (!res.ok) return;

        const favorites = await res.json();

        isFavoriteRecipe = favorites.some(r => r.id === recipeId);

        updateFavoriteButton();

    } catch (err) {
        console.error("Favorite check failed", err);
    }
}

function updateFavoriteButton() {
    const btn = document.getElementById("favoriteBtn");
    if (!btn) return;

    if (isFavoriteRecipe) {
        btn.innerText = "❤️ Remove from Favorites";
        btn.classList.remove("btn-green");
        btn.style.backgroundColor = "#e74c3c";
    } else {
        btn.innerText = "🤍 Add to Favorites";
        btn.style.backgroundColor = "#4CAF50";
    }
}


async function loadFavorites() {
    if (!checkToken()) return;
    dashboardMenu.innerHTML = "";

    dashboardContent.innerHTML = "<h2>❤️ My Favorite Recipes</h2><p id='loadingText'>Loading...</p><div id='favoritesContainer'></div>";

    try {
        const res = await fetch(`${API_BASE}/recipes/favorites`, {

            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to load favorites");

        const favorites = await res.json();

        document.getElementById("loadingText")?.remove();

        if (!favorites || favorites.length === 0) {
            document.getElementById("favoritesContainer").innerHTML = "<p>No favorite recipes yet.</p>";
            return;
        }

        // Display favorites similar to displayRecipes
        let html = `<div class="recipe-list">`;
        favorites.forEach(recipe => {
            html += `
                <div class="recipe-card" id="recipeCard-${recipe.id}">
                    <h3 class="recipe-title">${recipe.title}</h3>
                    <p>${recipe.description || ""}</p>
                    <div class="recipe-actions">
                        <button class="btn view" onclick="viewRecipe(${recipe.id})">View</button>
                    </div>
                </div>
            `;
        });
        html += `</div>`;

        document.getElementById("favoritesContainer").innerHTML = html;

    } catch (err) {
        console.error(err);
        document.getElementById("favoritesContainer").innerHTML = `<p style="color:red">${err.message}</p>`;
    }
}

// ================= PROFILE =================
async function loadProfile() {
     dashboardMenu.innerHTML = "";
    if (!token) {
        alert("Session expired");
        return;
    }

    try {
        // 1️⃣ Fetch user info (email, role, username)
        const userRes = await fetch(`${API_BASE}/users/me`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        if (!userRes.ok) throw new Error("Failed to fetch user info");
        const user = await userRes.json();

        // 2️⃣ Fetch profile info (age, gender, height, weight, profileCompleted)
        const profileRes = await fetch(`${API_BASE}/profile`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        if (!profileRes.ok) throw new Error("Failed to fetch profile data");
        const profile = await profileRes.json();

        // 3️⃣ Show warning if profile incomplete
        let warningMsg = "";
        if (!profile.profileCompleted) {
            warningMsg = `<p style="color:red; font-weight:bold;">⚠️ Please complete your profile</p>`;
        }

        // 4️⃣ Render profile form
        dashboardContent.innerHTML = `
<div class="profile-card">
    <h2>👤 My Profile</h2>

    ${warningMsg ? `<p class="warning">${warningMsg}</p>` : ""}

    <div class="profile-field">
        <label>Email</label>
        <input type="email" value="${user.email}" disabled>
    </div>

    <div class="profile-field">
        <label>Role</label>
        <input type="text" value="${user.role}" disabled>
    </div>

    <div class="profile-field">
        <label>Username</label>
        <input id="profile-username" type="text" value="${user.username ?? ""}" disabled>
    </div>

    <div class="profile-field">
        <label>Age</label>
        <input id="profile-age" type="number" value="${profile.age ?? ""}" disabled>
    </div>

    <div class="profile-field">
        <label>Gender</label>
        <select id="profile-gender" disabled>
            <option value="MALE" ${profile.gender === "MALE" ? "selected" : ""}>Male</option>
            <option value="FEMALE" ${profile.gender === "FEMALE" ? "selected" : ""}>Female</option>
            <option value="OTHER" ${profile.gender === "OTHER" ? "selected" : ""}>Other</option>
        </select>
    </div>

    <div class="profile-field">
        <label>Height (cm)</label>
        <input id="profile-height" type="number" value="${profile.height ?? ""}" disabled>
    </div>

    <div class="profile-field">
        <label>Weight (kg)</label>
        <input id="profile-weight" type="number" value="${profile.weight ?? ""}" disabled>
    </div>

    <div class="profile-field">
        <label>BMI</label>
        <input id="bmi" type="text" value="${profile.bmi ?? "-"} (${profile.bmiCategory ?? "-"})" disabled>
    </div>

    <div class="profile-field">
        <label>BMR</label>
        <input id="bmr" type="text" value="${profile.bmr ?? "-"} kcal/day" disabled>
    </div>

    <div class="profile-field">
        <label>Activity Level</label>
        <select id="profile-activity" disabled>
            <option value="SEDENTARY" ${profile.activityLevel === "SEDENTARY" ? "selected" : ""}>Sedentary</option>
            <option value="LIGHT" ${profile.activityLevel === "LIGHT" ? "selected" : ""}>Light</option>
            <option value="MODERATE" ${profile.activityLevel === "MODERATE" ? "selected" : ""}>Moderate</option>
            <option value="ACTIVE" ${profile.activityLevel === "ACTIVE" ? "selected" : ""}>Active</option>
            <option value="VERY_ACTIVE" ${profile.activityLevel === "VERY_ACTIVE" ? "selected" : ""}>Very Active</option>
        </select>
    </div>

    <div class="profile-field">
        <label>Goal</label>
        <select id="profile-goal" disabled>
            <option value="MAINTAIN" ${profile.goal === "MAINTAIN" ? "selected" : ""}>Maintain Weight</option>
            <option value="LOSE" ${profile.goal === "LOSE" ? "selected" : ""}>Weight Loss</option>
            <option value="GAIN" ${profile.goal === "GAIN" ? "selected" : ""}>Weight Gain</option>
        </select>
    </div>

    <div class="profile-field">
        <label>Goal Intensity</label>
        <select id="profile-intensity" disabled>
            <option value="MILD" ${profile.goalIntensity === "MILD" ? "selected" : ""}>Mild</option>
            <option value="STANDARD" ${profile.goalIntensity === "STANDARD" ? "selected" : ""}>Standard</option>
            <option value="AGGRESSIVE" ${profile.goalIntensity === "AGGRESSIVE" ? "selected" : ""}>Aggressive</option>
        </select>
    </div>

    

    <div class="profile-field">
        <label>Maintain Calories</label>
        <input id="maintain-cal" disabled>
    </div>

    <div class="profile-field">
        <label>Daily Calories</label>
        <input id="daily-cal" disabled>
    </div>

    <div class="profile-buttons">
        <button id="profile-edit-btn" class="btn-primary">Edit Profile</button>
        <button id="profile-save-btn" class="btn-success" style="display:none">Save Profile</button>
    </div>
</div>
`;

        // 5️⃣ Attach event listeners
        document.getElementById("profile-edit-btn").addEventListener("click", enableProfileEdit);
        document.getElementById("profile-save-btn").addEventListener("click", saveProfile);

        // 6️⃣ Attach auto-calculation
        attachProfileAutoCalculation();
        updateProfileCalculations();

    } catch (err) {
        console.error("Profile load error:", err);
        dashboardContent.innerHTML = `<p style="color:red">Failed to load profile</p>`;
    }
}

// ================= PROFILE AUTO-CALCULATION =================
function calculateBMI(weight, height) {
    if (!weight || !height) return 0;
    const heightM = height / 100;
    return (weight / (heightM * heightM)).toFixed(1);
}

function calculateBMR(weight, height, age, gender) {
    if (!weight || !height || !age || !gender) return 0;
    if (gender === "MALE") return Math.round(10 * weight + 6.25 * height - 5 * age + 5);
    if (gender === "FEMALE") return Math.round(10 * weight + 6.25 * height - 5 * age - 161);
    return Math.round(10 * weight + 6.25 * height - 5 * age); // OTHER
}

function calculateCalories(bmr, activityLevel, goal, goalIntensity) {
    if (!bmr) return 0;
    const activityMultiplier = {
        SEDENTARY: 1.2,
        LIGHT: 1.375,
        MODERATE: 1.55,
        ACTIVE: 1.725,
        VERY_ACTIVE: 1.9
    }[activityLevel] || 1.2;

    let calories = bmr * activityMultiplier;

    const goalFactor = {
        MAINTAIN: 0,
        LOSE: { MILD: -250, STANDARD: -500, AGGRESSIVE: -750 },
        GAIN: { MILD: 250, STANDARD: 500, AGGRESSIVE: 750 }
    };

    if (goal === "LOSE") calories += goalFactor.LOSE[goalIntensity] || -500;
    if (goal === "GAIN") calories += goalFactor.GAIN[goalIntensity] || 500;

    return Math.round(calories);
}

function attachProfileAutoCalculation() {
    const fields = ["profile-age","profile-height","profile-weight","profile-gender","profile-activity","profile-goal","profile-intensity"];
    fields.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener("input", updateProfileCalculations);
    });
}

function updateProfileCalculations() {
    const age = Number(document.getElementById("profile-age").value);
    const height = Number(document.getElementById("profile-height").value);
    const weight = Number(document.getElementById("profile-weight").value);
    const gender = document.getElementById("profile-gender").value.toUpperCase();
    const activity = document.getElementById("profile-activity").value.toUpperCase();
    const goal = document.getElementById("profile-goal").value.toUpperCase();
    const intensity = document.getElementById("profile-intensity").value.toUpperCase();

    if (!age || !height || !weight || !gender) return;

    const bmi = calculateBMI(weight, height);
    const bmr = calculateBMR(weight, height, age, gender);
    const dailyCalories = calculateCalories(bmr, activity, goal, intensity);
    const maintainCalories = Math.round(bmr * ({SEDENTARY:1.2,LIGHT:1.375,MODERATE:1.55,ACTIVE:1.725,VERY_ACTIVE:1.9}[activity] || 1.2));

    document.getElementById("bmi").value = bmi;
    document.getElementById("bmr").value = bmr + " kcal/day";
    document.getElementById("maintain-cal").value = maintainCalories;
    document.getElementById("daily-cal").value = dailyCalories;
}


// Enable editing
function enableProfileEdit() {
    ["profile-username", "profile-age", "profile-gender", "profile-height", "profile-weight","profile-activity", "profile-goal",  "profile-intensity"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.disabled = false;
    });

    document.getElementById("profile-edit-btn").style.display = "none";
    document.getElementById("profile-save-btn").style.display = "inline-block";
}

// Save profile
async function saveProfile() {
    const usernameEl = document.getElementById("profile-username");
    const ageEl = document.getElementById("profile-age");
    const genderEl = document.getElementById("profile-gender");
    const heightEl = document.getElementById("profile-height");
    const weightEl = document.getElementById("profile-weight");
    const activityEl = document.getElementById("profile-activity");
    const goalEl = document.getElementById("profile-goal");
    const intensityEl = document.getElementById("profile-intensity");

    if (!usernameEl || !ageEl || !genderEl || !heightEl || !weightEl || !activityEl || !goalEl || !intensityEl) {
        alert("Profile fields not loaded properly");
        return;
    }

    const username = usernameEl.value.trim();
    const age = Number(ageEl.value);
    const gender = genderEl.value.toUpperCase(); // MALE / FEMALE
    const height = Number(heightEl.value);
    const weight = Number(weightEl.value);
    const activityLevel = activityEl.value.toUpperCase(); // SEDENTARY, LIGHT, etc.
    const goal = goalEl.value.toUpperCase();             // MAINTAIN, LOSE, GAIN
    const goalIntensity = intensityEl.value.toUpperCase(); // MILD, STANDARD, AGGRESSIVE

    if (!username || age <= 0 || height <= 0 || weight <= 0) {
        alert("Please fill all fields correctly");
        return;
    }

    try {
        // ✅ Update profile including username in one call
        const profileRes = await fetch(`${API_BASE}/profile`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                username,
                age,
                gender,
                height,
                weight,
                activityLevel,
                goal,
                goalIntensity,
                profileCompleted: true
            })
        });

        if (!profileRes.ok) {
            const errText = await profileRes.text();
            throw new Error(`Profile update failed: ${errText}`);
        }

        // Reattach listeners for auto-calculation
        attachProfileAutoCalculation();

        alert("✅ Profile updated successfully!");

    } catch (err) {
        console.error(err);
        alert("❌ " + err.message);
    }
}

async function loadComments(recipeId) {
    const commentsSection = document.getElementById(`commentSection-${recipeId}`);
    if (!commentsSection) return;

    let container = document.getElementById(`commentsList-${recipeId}`);
    if (!container) {
        container = document.createElement("div");
        container.id = `commentsList-${recipeId}`;
        commentsSection.appendChild(container);
    }

    container.innerHTML = "<p>Loading comments...</p>";

    try {
        const res = await fetch(`${API_BASE}/comments/recipe/${recipeId}`, {

            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!res.ok) throw new Error("Failed to load comments");

        const comments = await res.json();
        container.innerHTML = "";

        if (!comments || comments.length === 0) {
            container.innerHTML = "<p>No comments yet.</p>";
            return;
        }

        // const loggedUserId = String(localStorage.getItem("userId"));
        // const loggedRole = localStorage.getItem("role"); // ADMIN / USER

       comments.forEach(c => {
    const canDelete = c.isOwner || c.isAdmin;
    const hasReplies = c.replies && c.replies.length > 0;
    const displayName = c.isOwner ? "You" : c.username;

    container.innerHTML += `
        <div class="comment-block" id="comment-${c.id}">
            <p><b>${displayName}</b>: ${c.content}</p>

            <div>
                <button onclick="showReplyForm(${recipeId}, ${c.id})">Reply</button>
                ${canDelete ? `<button onclick="deleteComment(${recipeId}, ${c.id})" style="color:red;">Delete</button>` : ""}
                ${hasReplies ? `<button onclick="toggleReplies(${c.id})">View Replies (${c.replies.length})</button>` : ""}
            </div>

            <div id="replies-${c.id}" style="display:none; margin-left:20px;">
                ${renderReplies(c.replies || [], recipeId)}
            </div>
        </div>
    `;
});

 } catch (err) {
        console.error("Comment load error:", err);
        container.innerHTML = "<p style='color:red'>Failed to load comments</p>";
    }
}
function toggleReplies(commentId) {
    const repliesDiv = document.getElementById(`replies-${commentId}`);
    if (!repliesDiv) return;

    repliesDiv.style.display =
        repliesDiv.style.display === "none" ? "block" : "none";
}

function renderReplies(replies, recipeId) {
    const loggedUserId = String(localStorage.getItem("userId"));
    const loggedRole = localStorage.getItem("role");

    return replies.map(r => {
        const isOwner = String(r.userId) === loggedUserId; // ✅ FIX
        const isAdmin = loggedRole === "ADMIN";
        const canDelete = isOwner || isAdmin;
        const hasReplies = r.replies && r.replies.length > 0;

        return `
            <div style="margin-left:15px; border-left:1px solid #ccc; padding-left:10px;">
                <p><b>${isOwner ? "You" : r.username}</b>: ${r.content}</p>

                <div>
                    <button onclick="showReplyForm(${recipeId}, ${r.id})">Reply</button>
                    ${canDelete ? `<button onclick="deleteComment(${recipeId}, ${r.id})" style="color:red;">Delete</button>` : ""}
                    ${hasReplies ? `<button onclick="toggleReplies(${r.id})">View Replies (${r.replies.length})</button>` : ""}
                </div>

                <div id="replies-${r.id}" style="display:none; margin-left:20px;">
                    ${renderReplies(r.replies || [], recipeId)}
                </div>
            </div>
        `;
    }).join("");
}
function postComment(recipeId) {
    const textarea = document.getElementById(`commentInput-${recipeId}`);
    if (!textarea) {
        console.error("commentInput not found", recipeId);
        return;
    }

    const content = textarea.value.trim();
    if (!content) {
        alert("Comment cannot be empty");
        return;
    }

   fetch(`${API_BASE}/comments/${recipeId}`, {

        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ content })
    })
    .then(res => {
        if (!res.ok) throw new Error("Failed to post comment");
        textarea.value = "";
        loadComments(recipeId);
    })
    .catch(err => {
        console.error(err);
        alert(err.message);
    });
}
function showReplyForm(recipeId, commentId) {
    const container = document.getElementById(`replies-${commentId}`);
    if (!container) return;

    // 🔥 MAKE SURE REPLIES ARE VISIBLE
    container.style.display = "block";

    // prevent multiple forms
    if (container.querySelector(".reply-form")) return;

    const form = document.createElement("div");
    form.className = "reply-form";
    form.innerHTML = `
        <textarea rows="2" style="width:100%;" placeholder="Write a reply..."></textarea>
        <button>Reply</button>
    `;

    container.prepend(form);

    const textarea = form.querySelector("textarea");
    const btn = form.querySelector("button");

    btn.onclick = async () => {
        const content = textarea.value.trim();
        if (!content) return alert("Reply cannot be empty");

        try {
            const res = await fetch(`${API_BASE}/comments/${recipeId}`, {

                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify({
                    content,
                    parentId: commentId
                })
            });

            if (!res.ok) throw new Error("Reply failed");

            loadComments(recipeId);
        } catch (err) {
            alert("Reply not sent");
            console.error(err);
        }
    };
}

function deleteComment(recipeId, commentId) {
    if (!confirm("Delete your comment?")) return;

    fetch(`${API_BASE}/comments/${commentId}`, {

        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` }
    })
    .then(res => {
        if (!res.ok) throw new Error("Delete failed");
        loadComments(recipeId);
    })
    .catch(err => {
        console.error(err);
        alert("Delete failed");
    });
}

function toggleComments(recipeId) {
    const section = document.getElementById(`commentSection-${recipeId}`);

    if (!section) {
        console.error("Comment section not found", recipeId);
        return;
    }

    const isHidden =
        section.style.display === "none" || section.style.display === "";

    if (isHidden) {
        section.style.display = "block";
        loadComments(recipeId); // load comments when opened
    } else {
        section.style.display = "none";
    }
}

// ----------------- Diet Log Section -----------------
// ----------------- Display Logs -----------------
function displayDietLogs(logs) {
    if (!logs || logs.length === 0) {
        dashboardContent.innerHTML = "";
        return;
    }

    let html = `
        <h2>Diet Logs</h2>
        <table class="diet-table">
            <thead>
                <tr>
                    <th>Date</th>
                    <th>Meal</th>
                    <th>Food Item</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
    `;

    logs.forEach(log => {
        html += `
            <tr>
                <td>${log.date}</td>
                <td>${log.meal}</td>
                <td>${log.foodItem}</td>
                <td>
                    <button onclick="editDietLog(${log.id}, '${log.date}', '${log.meal}', '${log.foodItem}')">Edit</button>
                </td>
            </tr>
        `;
    });

    html += `
            </tbody>
        </table>
    `;

    dashboardContent.innerHTML = html;
}

// ----------------- Fetch Logs -----------------
async function fetchAllLogs() {
    try {
        dashboardContent.innerHTML = "<p>Loading all logs...</p>";
        const res = await fetch(`${API_BASE}/diet/all`, { headers: authHeader() });
        const logs = await res.json();
        displayDietLogs(logs);
    } catch (err) {
        console.error(err);
        dashboardContent.innerHTML = "<p>Error fetching all logs.</p>";
    }
}

async function fetchWeeklyLogs() {
    try {
        dashboardContent.innerHTML = "<p>Loading weekly logs...</p>";
        const res = await fetch(`${API_BASE}/diet/weekly`, { headers: authHeader() });
        const logs = await res.json();
        displayDietLogs(logs);
    } catch (err) {
        console.error(err);
        dashboardContent.innerHTML = "<p>Error fetching weekly logs.</p>";
    }
}

async function fetchMonthlyLogs() {
    try {
        dashboardContent.innerHTML = "<p>Loading monthly logs...</p>";
        const res = await fetch(`${API_BASE}/diet/monthly`, { headers: authHeader() });
        const logs = await res.json();
        displayDietLogs(logs);
    } catch (err) {
        console.error(err);
        dashboardContent.innerHTML = "<p>Error fetching monthly logs.</p>";
    }
}

// ----------------- Add / Update Diet Log -----------------
function renderAddDietLogForm(log = null) {
    const html = `
        <h2>Add / Update Diet Log</h2>
        <form id="dietLogForm">
            <input type="hidden" id="dietId" value="${log ? log.id : ''}" />

            <label>Date:</label>
            <input type="date" id="dietDate" value="${log ? log.date : new Date().toISOString().split('T')[0]}" required />

            <label>Meal:</label>
            <input type="text" id="dietMeal" placeholder="Breakfast, Lunch, Dinner..." value="${log ? log.meal : ''}" required />

            <label>Food Item:</label>
            <input type="text" id="dietFood" placeholder="What you ate..." value="${log ? log.foodItem : ''}" required />

            <button type="submit">${log ? 'Update' : 'Save'}</button>
        </form>
        <div id="dietFormMsg"></div>
    `;

    dashboardContent.innerHTML = html;

    document.getElementById("dietLogForm").addEventListener("submit", async (e) => {
        e.preventDefault();
        await submitDietLog();
    });
}

// ----------------- Submit Diet Log -----------------
async function submitDietLog() {
    const id = document.getElementById("dietId").value;
    const date = document.getElementById("dietDate").value;
    const meal = document.getElementById("dietMeal").value;
    const foodItem = document.getElementById("dietFood").value;

    // --- Frontend validation ---
    if (!date || !meal || !foodItem) {
        alert("Please fill all fields!");
        return;
    }

    const payload = { date, meal, foodItem }; // matches backend DietLog entity
            if (id) payload.id = id;
    const url = `${API_BASE}/diet/add`;
    const method = "POST";

    try {
        const res = await fetch(url, {
            method,
            headers: authHeader(),
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            // Read error message from backend
            const errData = await res.text();
            throw new Error(errData);
        }

        const data = await res.json();
        console.log("Diet log saved:", data);
        document.getElementById("dietFormMsg").innerText = "Diet log saved successfully!";
        fetchAllLogs(); // Refresh the logs
    } catch (err) {
        console.error("Error saving diet log:", err);
        document.getElementById("dietFormMsg").innerText = "Error saving diet log: " + err.message;
    }
}
window.editDietLog = function(id, date, meal, foodItem) {
    renderAddDietLogForm({ id, date, meal, foodItem });
};

