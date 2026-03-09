console.log("admin.js loaded successfully");
const USDA_API_KEY ="${USDA_API_KEY}";
const API_BASE = "http://localhost:8081/api/admin";
const RECIPE_API = "http://localhost:8081/api/recipes";
const COMMENT_API = "http://localhost:8081/api/comments";


const token = localStorage.getItem("token");
const userEmail = localStorage.getItem("email"); // ✅ get email stored at login

let recognition = null;
let activeVoiceField = null;
let dashboardContent;
let currentRecipeView = ""; 
let nutritionData = {
    calories: 0,
    protein: 0,
    carbs: 0,
    fat: 0
};


/* ===============================
   PAGE LOAD
================================ */
document.addEventListener("DOMContentLoaded", () => {
        const usernameSpan = document.getElementById("username");
    if (usernameSpan) {
        const username = localStorage.getItem("username") || "User";
        usernameSpan.textContent = username;
    }

    dashboardContent = document.getElementById("dashboard-content");

    if (!dashboardContent) {
        console.error("❌ dashboard-content not found");
        return;
    }

    // Sidebar buttons
    document.getElementById("btn-users")?.addEventListener("click", loadUsers);
    document.getElementById("btn-other-recipes")?.addEventListener("click", loadOtherRecipes);
    document.getElementById("btn-my-recipes")?.addEventListener("click", loadMyRecipes);
    document.getElementById("btn-add-recipe")?.addEventListener("click", toggleAddRecipeForm);
    document.getElementById("btn-unapproved")?.addEventListener("click", loadUnapproved);
    document.getElementById("btn-rejected")?.addEventListener("click", loadRejected);
    

    document.getElementById("btn-comments")?.addEventListener("click", loadComments);
    document.getElementById("statsBtn")?.addEventListener("click", showStats);
    

    document.getElementById("btn-logout")?.addEventListener("click", (e) => {
    e.preventDefault(); // 🔥 REQUIRED for <a href="#">
    logout();
});

  
    dashboardContent.innerHTML = "<p>Select a section from the menu</p>";
});

/* ===============================
   AUTH CHECK
================================ */
function checkToken() {
    if (!token) {
        dashboardContent.innerHTML =
            "<p style='color:red'>No token found. Please login.</p>";
        return false;
    }
    return true;
}

/* ===============================
   LOGOUT
================================ */
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("email");
    window.location.href = "login.html";
}

/* ===============================
   USERS LIST
================================ */
async function loadUsers() {
    if (!checkToken()) return;

    dashboardContent.innerHTML = "<p>Loading users...</p>";

    try {
        const res = await fetch(`${API_BASE}/users`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to load users");

        const users = await res.json();
        renderUsers(users);

    } catch (err) {
        dashboardContent.innerHTML = `<p style="color:red">${err.message}</p>`;
    }
}
/*=======================
Render users
=======================*/

function renderUsers(users) {
    if (!users || users.length === 0) {
        dashboardContent.innerHTML = "<p>No users found.</p>";
        return;
    }

    const totalUsers = users.length;
    const totalAdmins = users.filter(u => u.role.toLowerCase() === "admin").length;
    const totalNormalUsers = totalUsers - totalAdmins;

    const loggedInEmail = (localStorage.getItem("email") || "").trim().toLowerCase();

    let html = `
        <h2>Users</h2>
        <div style="margin-bottom:15px;">
            <strong>Total Users:</strong> ${totalUsers} |
            <strong>Admins:</strong> ${totalAdmins} |
            <strong>Normal Users:</strong> ${totalNormalUsers}
        </div>

        <table>
            <thead>
                <tr>
                    <th>S.No</th>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Recipes Count</th>
                </tr>
            </thead>
            <tbody>
                ${users.map((user, index) => `
                    <tr style="${user.email.toLowerCase() === loggedInEmail ? 'background:#e8f8f5;font-weight:bold;' : ''}">
                        <td>${index + 1}</td>
                        <td>${user.username} ${user.email.toLowerCase() === loggedInEmail ? '<span style="color:green;">(Me)</span>' : ''}</td>
                        <td>${user.email}</td>
                        <td>${user.role}</td>
                        <td>${user.recipeCount ?? 0}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;

    dashboardContent.innerHTML = html;
}

/* ===============================
   OTHER USERS' RECIPES
================================ */
async function loadOtherRecipes() {
    if (!checkToken()) return;
    currentRecipeView = "OTHER";
    dashboardContent.innerHTML = "<h2>Other Recipes</h2><p>Loading...</p>";

    try {
        const res = await fetch(`${API_BASE}/recipes/other`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to load recipes");

        const recipes = await res.json();
        displayRecipes(recipes);

    } catch (err) {
        dashboardContent.innerHTML = `<p style="color:red">${err.message}</p>`;
    }
}

/* ===============================
   MY RECIPES
================================ */
async function loadMyRecipes() {
    if (!checkToken()) return;
   currentRecipeView = "MY_RECIPES";

    if (!userEmail) {
        dashboardContent.innerHTML = "<p style='color:red'>Email not found. Please login again.</p>";
        return;
    }

   
    dashboardContent.innerHTML = `
    <h2>My Recipes</h2>
    <p id="loadingText">Loading...</p>
    <div id="recipeContainer"></div>
`;


    try {
        const res = await fetch(`${API_BASE}/my-recipes?email=${encodeURIComponent(userEmail)}`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to load my recipes");

        const recipes = await res.json();
        displayRecipes(recipes);
         document.getElementById("loadingText")?.remove();

    } catch (err) {
            document.getElementById("loadingText")?.remove();
            dashboardContent.innerHTML += `<p style="color:red">${err.message}</p>`;
    }
}

/* ===============================
   DISPLAY RECIPES
================================ */
function displayRecipes(recipes) {
    if (!recipes || recipes.length === 0) {
        dashboardContent.innerHTML = "<p>No recipes found.</p>";
        return;
    }

    const isAdmin = localStorage.getItem("role") === "ADMIN";
    const isUnapprovedView = currentRecipeView === "UNAPPROVED";

    let html = `<div class="recipe-list">`;

    recipes.forEach(recipe => {
      
        const isPending = recipe.approved !== true;
        const isUserRecipe = recipe.createdBy?.role === "USER";


        console.log({
  approved: recipe.approved,
  createdBy: recipe.createdBy,
  role: localStorage.getItem("role"),
  currentRecipeView
});
const showModerationButtons =
            isAdmin && isUnapprovedView && isUserRecipe && isPending;
const isMyRecipeView = currentRecipeView === "MY_RECIPES";
        html += `
            <div class="recipe-card" id="recipeCard-${recipe.id}">
                <h3>${recipe.title}</h3>
                ${
    !isMyRecipeView
        ? `<p><b>Created by:</b> ${recipe.createdByUsername}</p>`
        : ""
}

                <p>📅 ${formatDateTime(recipe.createdAt)}</p>

                <button class="btn view" onclick="viewRecipe(${recipe.id})">
                    View
                </button>

                ${
                    !isUnapprovedView
                        ? `
                            <button class="btn edit" onclick="editRecipe(${recipe.id})">
                                Edit
                            </button>
                            <button class="btn delete" onclick="deleteRecipe(${recipe.id})">
                                Delete
                            </button>
                            <button class="btn comment" onclick="toggleComments(${recipe.id})">
                                Comments
                            </button>

                            <div id="commentSection-${recipe.id}" style="display:none; margin-top:10px;">
                                <div id="commentsList-${recipe.id}"></div>
                                <textarea id="commentInput-${recipe.id}" placeholder="Write a comment..."></textarea>
                                <button class="btn" onclick="postComment(${recipe.id})"style="background-color:green;color:white;"  >
                                    Post
                                </button>
                            </div>
                          `
                        : ""
                }

                ${
                    showModerationButtons
                        ? `
                            <button class="btn approve" onclick="approveRecipe(${recipe.id})">
                                Approve
                            </button>
                            <button class="btn reject" onclick="rejectRecipe(${recipe.id})">
                                Reject
                            </button>
                          `
                        : ""
                }
            </div>
        `;
    });

    html += `</div>`;

    let container = document.getElementById("recipeContainer");
    if (!container) {
        dashboardContent.innerHTML = "";
        container = document.createElement("div");
        container.id = "recipeContainer";
        dashboardContent.appendChild(container);
    }

    container.innerHTML = html;
}


/* ===============================
   ADD RECIPE
================================ */
// function toggleAddRecipeForm() {
//     if (!checkToken()) return;

//     dashboardContent.innerHTML = `
//         <h2>Add New Recipe</h2>
//         <form id="addRecipeForm" style="max-width:600px;">
//          <div id="aiStatus" style="color: green; margin-bottom: 10px;"></div>
//             <label>Name</label>
//             <input type="text" id="recipeName" required>
// <button type="button" id="aiGenerateIngredientsBtn">AI Generate Ingredients</button>
// <button type="button" id="aiGenerateDescriptionBtn">AI Generate Description</button>

//             <label>Ingredients</label>
//             <textarea id="ingredients"></textarea>
//              <button type="button" class="btn btn-green" onclick="startVoice('ingredients')">🎤 Ingredients Voice</button>
           
//             <label>Description</label>
//             <textarea id="description"></textarea>
//            <button type="button" class="btn btn-green" onclick="startVoice('description')">
//            🎤 Description Voice</button>

            
//             <label>Nutrition</label>
//             <textarea id="nutrition" readonly></textarea>

//             <button type="button" class="btn btn-green" onclick="fetchNutrition()">Fetch Nutrition</button>
//             <br><br>
            
//     <div class="form-group">
//     <label for="cookingTime">Cooking Time (minutes)</label>
//     <input 
//         type="number" 
//         id="cookingTime" 
//         name="cookingTime"  
//         min="1"
//         required
//         class="form-control"
//     >
// </div>
  
// <button type="submit" id="saveBtn" class="btn btn-green">Save</button>

//         </form>
//     `;

//     document.getElementById("addRecipeForm").addEventListener("submit", async (e) => {
//         e.preventDefault();
//         await saveRecipe();
//     });
    
//      }

// const recipeNameInput = document.getElementById("recipeName");
// const ingredientsBox = document.getElementById("ingredients");
// const descriptionBox = document.getElementById("description");
// const statusBox = document.getElementById("aiStatus"); // make sure this exists in HTML

// // 1️⃣ Generate Ingredients
// document.getElementById("aiGenerateIngredientsBtn").addEventListener("click", async (e) => {
//     e.preventDefault();
//     const recipeName = recipeNameInput.value.trim();
//     if (!recipeName) return alert("Enter a recipe name!");

//     statusBox.textContent = "Generating ingredients...";

//     try {
//         const res = await fetch(`${API_BASE}/recipes/generate`, {
//             method: "POST",
//             headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
//             body: JSON.stringify({ recipeName })
//         });
//         if (!res.ok) throw new Error(await res.text());
//         const data = await res.json();

//         // Fill ingredients textarea
//         const ingredientsText = Array.from(data.ingredients).map(i => `${i.name} ${i.quantity} ${i.unit}`).join("\n");
//         ingredientsBox.value = ingredientsText;

//         // Fill description too if available
//         if (data.description) descriptionBox.value = data.description;

//         statusBox.textContent = "Ingredients generated. You can edit or add/remove items.";
//     } catch (err) {
//         console.error(err);
//         statusBox.textContent = "Failed to generate ingredients: " + err.message;
//     }
// });

// // 2️⃣ Generate Description only (optional)
// document.getElementById("aiGenerateDescriptionBtn").addEventListener("click", async (e) => {
//     e.preventDefault();
//     const recipeName = recipeNameInput.value.trim();
//     if (!recipeName) return alert("Enter recipe name first!");

//     const ingredientLines = ingredientsBox.value
//         .split("\n")
//         .map(line => line.trim())
//         .filter(line => line.length > 0);

//     if (!ingredientLines.length) return alert("Ingredients cannot be empty!");

//     statusBox.textContent = "Generating description...";

//     try {
//         const res = await fetch(`${API_BASE}/recipes/generate`, {
//             method: "POST",
//             headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
//             body: JSON.stringify({ recipeName })
//         });
//         if (!res.ok) throw new Error(await res.text());
//         const data = await res.json();

//         descriptionBox.value = data.description || "";
//         statusBox.textContent = "Description generated successfully!";
//     } catch (err) {
//         console.error(err);
//         statusBox.textContent = "Failed to generate description: " + err.message;
//     }
// });

// // 3️⃣ Save Recipe
// document.getElementById("saveBtn").addEventListener("click", async () => {
//     const title = recipeNameInput.value.trim();
//     const description = descriptionBox.value.trim();

//     const ingredients = parseIngredients(ingredientsBox.value);
//     if (!title || !description || ingredients.length === 0)
//         return alert("Fill all fields before saving!");

//     statusBox.textContent = "Saving recipe...";

//     try {
//         const res = await fetch(`${API_BASE}/recipes/generate`, { // use same endpoint
//             method: "POST",
//             headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
//             body: JSON.stringify({ recipeName: title }) // recipeName required
//         });
//         if (!res.ok) throw new Error(await res.text());
//         statusBox.textContent = "Recipe saved successfully!";
//         alert("✅ Recipe saved!");
//         ingredientsBox.value = "";
//         descriptionBox.value = "";
//         recipeNameInput.value = "";
//     } catch (err) {
//         console.error(err);
//         statusBox.textContent = "Failed to save recipe: " + err.message;
//     }
// });

// // Correct ingredients parsing
// function parseIngredients(text) {
//     const lines = text.split("\n");
//     const ingredients = [];

//     for (let line of lines) {
//         line = line.trim();
//         if (!line) continue;

//         const match = line.match(/(.+?)\s+([\d.]+)\s*(\w+)?$/);
//         if (match) {
//             ingredients.push({
//                 name: match[1].trim(),
//                 quantity: parseFloat(match[2]),
//                 unit: match[3] ? match[3].trim() : "g"
//             });
//         }
//     }

//     return ingredients;
// }
function toggleAddRecipeForm() {
    if (!checkToken()) return;

    dashboardContent.innerHTML = `
        <h2>Add New Recipe</h2>
        <form id="addRecipeForm" style="max-width:600px;">
            <div id="aiStatus" style="color: green; margin-bottom: 10px;"></div>

            <label>Name</label>
            <input type="text" id="recipeName" required>
            <button type="button" id="aiGenerateIngredientsBtn" class="btn btn-green">AI Generate Ingredients</button>
            <button type="button" id="aiGenerateDescriptionBtn" class="btn btn-green">AI Generate Description</button>

            <label>Ingredients</label>
            <textarea id="ingredients"></textarea>
            <button type="button" class="btn btn-green" onclick="startVoice('ingredients')">🎤 Ingredients Voice</button>

            <label>Description</label>
            <textarea id="description"></textarea>
            <button type="button" class="btn btn-green" onclick="startVoice('description')">🎤 Description Voice</button>

            <label>Nutrition</label>
            <textarea id="nutrition" readonly></textarea>
            <button type="button" class="btn btn-green" onclick="fetchNutrition()">Fetch Nutrition</button>

            <div class="form-group" style="margin-top: 10px;">
                <label for="cookingTime">Cooking Time (minutes)</label>
                <input type="number" id="cookingTime" name="cookingTime" min="1" required class="form-control">
            </div>

          <button type="button" id="saveRecipeBtn" class="btn btn-green" style="margin-top: 10px;">Save Recipe</button>
        </form>
    `;

    // ---------- DOM ELEMENTS ----------
    const recipeNameInput = document.getElementById("recipeName");
    const ingredientsBox = document.getElementById("ingredients");
    const descriptionBox = document.getElementById("description");
    const statusBox = document.getElementById("aiStatus");
    const aiGenerateIngredientsBtn = document.getElementById("aiGenerateIngredientsBtn");
    const aiGenerateDescriptionBtn = document.getElementById("aiGenerateDescriptionBtn");
    const saveRecipeBtn = document.getElementById("saveRecipeBtn");

    // ---------- HELPER: FETCH WITH TIMEOUT ----------
    function fetchWithTimeout(url, options = {}, timeout = 20000) {
        return Promise.race([
            fetch(url, options),
            new Promise((_, reject) => setTimeout(() => reject(new Error("Request timeout")), timeout))
        ]);
    }

    function showStatus(message, type = "info", duration = 3000) {
    statusBox.innerText = message;

    statusBox.className = "status " + type; // optional styling

    if (duration > 0) {
        setTimeout(() => {
            statusBox.innerText = "";
            statusBox.className = "status";
        }, duration);
    }
}

    // -------------------------------
    // 1️⃣ GENERATE INGREDIENTS
    // -------------------------------
    // async function generateIngredients() {
    //     const recipeName = recipeNameInput.value.trim();
    //     if (!recipeName) return alert("Enter recipe name!");

    //     statusBox.innerText = "Generating ingredients...";

    //     try {
    //         const res = await fetchWithTimeout(
    //             "http://localhost:8081/api/recipes/generate-ingredients",
    //             {
    //                 method: "POST",
    //                 headers: {
    //                     "Content-Type": "application/json",
    //                     Authorization: `Bearer ${token}`
    //                 },
    //                 body: JSON.stringify({ recipeName })
    //             },
    //             25000
    //         );

    //         if (!res.ok) throw new Error(await res.text());

    //         const data = await res.json();
    //         ingredientsBox.value = data.map(i => `${i.name} ${i.quantity} ${i.unit}`).join("\n");

    //         statusBox.innerText = "Ingredients generated successfully. You can edit them if needed.";

    //     } catch (err) {
    //         console.error(err);
    //         statusBox.innerText = "Failed to generate ingredients.";
    //         alert("AI is taking too long. Try again.");
    //     }
    // }
    async function generateIngredients() {
    const recipeName = recipeNameInput.value.trim();
    if (!recipeName) {
        showStatus("Please enter a recipe name first 😊", "error");
        return;
    }

    showStatus("Generating ingredients… ⏳", "loading", 0);

    try {
        const res = await fetchWithTimeout(
            "http://localhost:8081/api/recipes/generate-ingredients",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify({ recipeName })
            },
            25000
        );

        if (!res.ok) throw new Error(await res.text());

        const data = await res.json();
        ingredientsBox.value = data
            .map(i => `${i.name} ${i.quantity} ${i.unit}`)
            .join("\n");

        showStatus(
            "Ingredients generated successfully. You can edit them if needed ✨",
            "success"
        );

    } catch (err) {
        console.error(err);
        showStatus(
            "Oops! The AI is taking a little longer than usual. Please try again 🙂",
            "error"
        );
    }
}


    // -------------------------------
    // 2️⃣ GENERATE DESCRIPTION
    // -------------------------------
    // async function generateDescription() {
    //     const recipeName = recipeNameInput.value.trim();
    //     if (!recipeName) return alert("Enter recipe name first!");

    //     const ingredients = ingredientsBox.value
    //         .split("\n")
    //         .map(line => line.trim())
    //         .filter(line => line.length > 0);

    //     if (ingredients.length === 0) return alert("Ingredients cannot be empty!");

    //     statusBox.innerText = "Generating description...";

    //     try {
    //         const res = await fetchWithTimeout(
    //             "http://localhost:8081/api/recipes/generate-description",
    //             {
    //                 method: "POST",
    //                 headers: {
    //                     "Content-Type": "application/json",
    //                     Authorization: `Bearer ${token}`
    //                 },
    //                 body: JSON.stringify({ recipeName, ingredients })
    //             },
    //             25000
    //         );

    //         if (!res.ok) throw new Error(await res.text());

    //         const description = await res.text();
    //         descriptionBox.value = description;
    //         statusBox.innerText = "Description generated successfully.";

    //     } catch (err) {
    //         console.error(err);
    //         statusBox.innerText = "Failed to generate description.";
    //         alert("AI is taking too long. Try again.");
    //     }
    // }
async function generateDescription() {
    const recipeName = recipeNameInput.value.trim();
    if (!recipeName) {
        showStatus("Please enter a recipe name first 😊", "error");
        return;
    }

    const ingredients = ingredientsBox.value
        .split("\n")
        .map(line => line.trim())
        .filter(line => line.length > 0);

    if (ingredients.length === 0) {
        showStatus("Please add at least one ingredient before generating description 🥗", "error");
        return;
    }

    showStatus("Generating description… ✍️", "loading", 0);

    try {
        const res = await fetchWithTimeout(
            "http://localhost:8081/api/recipes/generate-description",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify({ recipeName, ingredients })
            },
            25000
        );

        if (!res.ok) throw new Error(await res.text());

        const description = await res.text();
        descriptionBox.value = description;

        showStatus("Description generated successfully 🎉", "success");

    } catch (err) {
        console.error(err);
        showStatus(
            "Sorry! The AI couldn’t generate the description right now. Please try again 🙏",
            "error"
        );
    }
}

    // -------------------------------
    // PARSE INGREDIENTS BEFORE SAVING
    // -------------------------------
    function parseIngredients(ingredientsText) {
        try {
            return ingredientsText.split("\n").map(line => {
                const parts = line.trim().split(" ");
                if (parts.length < 3) throw new Error("Each ingredient must have: name quantity unit");

                const quantity = Number(parts[parts.length - 2]);
                if (isNaN(quantity)) throw new Error("Quantity must be a number for " + parts[0]);

                const name = parts.slice(0, parts.length - 2).join(" ");
                const unit = parts[parts.length - 1];

                return { name, quantity, unit };
            });
        } catch (err) {
            alert("❌ Ingredient parsing error: " + err.message);
            console.error(err);
            return null;
        }
    }

    // -------------------------------
    // SAVE RECIPE
    // -------------------------------
    async function saveRecipe() {
        const recipeName = recipeNameInput.value.trim();
        const description = descriptionBox.value.trim();
        const cookingTime = Number(document.getElementById("cookingTime").value);
        const ingredientsText = ingredientsBox.value.trim();

        if (!recipeName || !description || cookingTime <= 0) {
            alert("Fill all required fields");
            return;
        }

        const ingredientArray = parseIngredients(ingredientsText);
        if (!ingredientArray || ingredientArray.length === 0) return;

        // AUTO FETCH NUTRITION IF NOT DONE
        if (!nutritionData || nutritionData.calories === 0) await fetchNutrition();

        ingredientArray.forEach(i => {
            i.calories = nutritionData.calories;
            i.protein = nutritionData.protein;
            i.carbs = nutritionData.carbs;
            i.fat = nutritionData.fat;
        });

        if (!nutritionData || nutritionData.calories === 0) {
            alert("Nutrition could not be calculated");
            return;
        }

        const recipeData = {
            title: recipeName,
            description,
            cookingTime,
            ingredients: ingredientArray,
            totalCalories: nutritionData.calories,
            totalProtein: nutritionData.protein,
            totalCarbs: nutritionData.carbs,
            totalFats: nutritionData.fat
        };

        try {
            const res = await fetch(`${API_BASE}/recipes`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify(recipeData)
            });

            if (!res.ok) throw new Error(await res.text());
            alert("✅ Recipe saved successfully!");
            loadMyRecipes();

        } catch (err) {
            console.error(err);
            alert("❌ " + err.message);
        }
    }

    // ---------- EVENT LISTENERS ----------
    aiGenerateIngredientsBtn.addEventListener("click", generateIngredients);
    aiGenerateDescriptionBtn.addEventListener("click", generateDescription);
    saveRecipeBtn.addEventListener("click", saveRecipe);
}


/* ===============================
   PLACEHOLDERS
================================ */
async function loadUnapproved() {
    if (!checkToken()) return;
    currentRecipeView = "UNAPPROVED";

    dashboardContent.innerHTML = `
        <h2>Unapproved Recipes</h2>
        <div id="recipeContainer">
            <p>Loading...</p>
        </div>
    `;

    try {
        const res = await fetch(`${API_BASE}/recipes/unapproved`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to load unapproved recipes");

        const recipes = await res.json();
        displayRecipes(recipes);

    } catch (err) {
        document.getElementById("recipeContainer").innerHTML =
            `<p style="color:red">${err.message}</p>`;
    }
}







/* ===============================
   HELPERS
================================ */
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
    const isAdmin = localStorage.getItem("role") === "ADMIN";
    const isUnapprovedView = currentRecipeView === "UNAPPROVED";
    const isPending = recipe.approved !== true; // false or null

    let ingredientsHtml = "No ingredients";
    if (Array.isArray(recipe.ingredients) && recipe.ingredients.length > 0) {
        ingredientsHtml = recipe.ingredients
            .map(i => `${i.name} - ${i.quantity ?? ""} ${i.unit ?? ""}`.trim())
            .join("\n");
    }

    let moderationButtons = "";
    if (isAdmin && isUnapprovedView && isPending) {
        moderationButtons = `
           <div class="moderation-actions">
    <button type="button" class="btn approve"
        onclick="approveAndNext(${recipe.id})">
        ✅ Approve
    </button>

    <button type="button" class="btn reject"
        onclick="rejectRecipe(${recipe.id})">
        ❌ Reject
    </button>
</div>

        `;
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

            <label>Cooking Time (minutes)</label>
            <input type="number" value="${recipe.cookingTime ?? 0}" readonly>

            ${moderationButtons}

            <button type="button" class="btn" onclick="loadUnapproved()">
                ⬅ Back
            </button>
        </form>
    `;
}
//===========================
// fetch nutrition
//===========================
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
        .map(line => line.trim())
        .filter(Boolean)
        .map(line => {

             const match = line.match(/^(.+?)\s+([\d.]+)\s*(g|kg|ml|l)$/i);

            if (!match) return null;

            return {
                name: match[1].trim(),
                quantity: parseFloat(match[2]),
                unit: match[3]
            };
        })
        .filter(Boolean);

    if (ingredients.length === 0) {
        alert("No valid ingredients found");
        return;
    }

    nutritionBox.value = "Fetching nutrition...";

    try {

        const res = await fetch("http://localhost:8081/api/nutrition/fetch",{
            method: "POST",
            headers: { "Content-Type": "application/json"   },
            body: JSON.stringify({ ingredients })
        });

        if (!res.ok) throw new Error("USDA failed");

        const data = await res.json();

        nutritionData = {
            calories: Math.round(data.calories),
            protein: Number(data.protein.toFixed(1)),
            carbs: Number(data.carbs.toFixed(1)),
            fat: Number(data.fat.toFixed(1))
        };

        nutritionBox.value =
`Calories: ${nutritionData.calories} kcal
Protein: ${nutritionData.protein} g
Carbs: ${nutritionData.carbs} g
Fat: ${nutritionData.fat} g`;

    } catch (err) {

        console.warn("USDA failed → backend", err);

        try {

            const fallbackRes = await fetch("http://localhost:8081/api/nutrition/fallback", {
                method: "POST",
                headers: { "Content-Type": "application/json"},
                body: JSON.stringify({ ingredients })
            });

            if (!fallbackRes.ok) throw new Error("Fallback failed");

            const data = await fallbackRes.json();

            nutritionData = {
                calories: Math.round(data.calories),
                protein: Number(data.protein.toFixed(1)),
                carbs: Number(data.carbs.toFixed(1)),
                fat: Number(data.fat.toFixed(1))
            };

            nutritionBox.value =
`Calories: ${nutritionData.calories} kcal
Protein: ${nutritionData.protein} g
Carbs: ${nutritionData.carbs} g
Fat: ${nutritionData.fat} g`;

        } catch (backendErr) {

            console.error("Backend failed", backendErr);
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
        nutritionData = {
    calories: recipe.totalCalories || 0,
    protein: recipe.totalProtein || 0,
    carbs: recipe.totalCarbs || 0,
    fat: recipe.totalFats || 0
};


        // Convert ingredient objects to textarea-friendly string: "name-quantity-unit"
        const ingredientsText = recipe.ingredients
                 .map(i => {
        const quantity = i.quantity != null ? i.quantity : ""; // only default if missing
        const unit = i.unit != null ? i.unit : "g";           // default only if missing
        return `${i.name} ${quantity} ${unit}`;
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
async function updateRecipe(id) {
    // 1️⃣ Get ingredients from textarea
    const ingredientsString = document.getElementById("ingredients").value.trim();
    if (!ingredientsString) {
        alert("❌ Enter ingredients");
        return;
    }

    // 2️⃣ Parse ingredients for payload (no need to attach total nutrition per ingredient)
    const ingredientArray = ingredientsString
        .split("\n")
        .map(line => line.trim())
        .filter(Boolean)
        .map(line => {
            const match = line.match(/^(.+?)\s*[-]?\s*([\d.]+)\s*(g|kg|ml|l)$/i);
            if (!match) return null;

            return {
                name: match[1].trim(),
                quantity: parseFloat(match[2]),
                unit: match[3],
                calories: nutritionData.calories,
            protein: nutritionData.protein,
            carbs: nutritionData.carbs,
            fat: nutritionData.fat
            };
        })
        .filter(Boolean);

    if (ingredientArray.length === 0) {
        alert("❌ No valid ingredients found.");
        return;
    }

    // 3️⃣ Ensure nutritionData has been fetched manually
    if (!nutritionData || nutritionData.calories === 0) {
        alert("❌ Please click 'Fetch Nutrition' before updating.");
        return;
    }

    // 4️⃣ Build payload
    const recipeData = {
        title: document.getElementById("title").value.trim(),
        description: document.getElementById("description").value.trim(),
        cookingTime: Number(document.getElementById("cookingTime").value),
        ingredients: ingredientArray,
        totalCalories: nutritionData.calories,
        totalProtein: nutritionData.protein,
        totalCarbs: nutritionData.carbs,
        totalFats: nutritionData.fat
    };

    console.log("🧪 UPDATE PAYLOAD:", recipeData);

    // 5️⃣ Send update request
    try {
        const res = await fetch(`${API_BASE}/recipes/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify(recipeData)
        });

        if (!res.ok) {
            const errText = await res.text();
            throw new Error(errText || "Update failed");
        }

        alert("✅ Recipe updated with correct nutrition!");
        loadMyRecipes();
    } catch (err) {
        console.error("❌ Update failed:", err);
        alert("❌ Update failed: " + err.message);
    }
}

//----------------------------------
// DELETE RECIPE  
//----------------------------------
async function deleteRecipe(recipeId, recipeOwnerId, recipeOwnerRole) {
    const token = localStorage.getItem("token");
    const userRole = localStorage.getItem("role"); // ADMIN / USER
    const userId = localStorage.getItem("userId");

    if (!recipeId) {
        alert("❌ Recipe ID missing");
        return;
    }

    if (!token) {
        alert("Session expired. Please login again.");
        return;
    }

    // USER: only own recipe
    if (userRole !== "ADMIN" && userId !== String(recipeOwnerId)) {
        alert("You can only delete your own recipes!");
        return;
    }

    // ADMIN: cannot delete another admin’s recipe
    if (
        userRole === "ADMIN" &&
        recipeOwnerRole === "ADMIN" &&
        userId !== String(recipeOwnerId)
    ) {
        alert("Admins cannot delete other admins' recipes!");
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
    let message = "Delete failed";

    try {
        const err = await res.json();
        message = err.error || message;
    } catch {
        message = await res.text();
    }

    alert("⚠️ " + message);
    return;
}


        alert("✅ Recipe deleted successfully!");

        // Remove card instantly
        const card = document.getElementById(`recipeCard-${recipeId}`);
        if (card) card.remove();

    } catch (err) {
        console.error(err);
        alert("❌ " + err.message);
    }

}


async function approveRecipe(recipeId) {
    try {
        const res = await fetch(`${API_BASE}/recipes/${recipeId}/approve`, {
            method: "PUT",
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to approve recipe");

        alert("✅ Recipe approved");
        loadUnapproved(); // refresh list
    } catch (err) {
        console.error(err);
        alert("Approval failed");
    }
}

async function approveAndNext(recipeId) {
    try {
        const res = await fetch(`${API_BASE}/recipes/${recipeId}/approve`, {
            method: "PUT",
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to approve recipe");

        alert("✅ Recipe approved");

        // Load the next unapproved recipe
        const listRes = await fetch(`${API_BASE}/recipes/unapproved`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        if (!listRes.ok) throw new Error("Failed to fetch unapproved recipes");

        const recipes = await listRes.json();
        if (recipes.length === 0) {
            dashboardContent.innerHTML = "<p>No unapproved recipes remaining.</p>";
            return;
        }

        // Show the first recipe in the list
        renderViewRecipeForm(recipes[0]);

    } catch (err) {
        console.error(err);
        alert("Approval failed: " + err.message);
    }
}


// ==============================
// Load Rejected Recipes List
// ==============================

async function loadRejected() {
    currentRecipeView = "REJECTED"; // track current view
    dashboardContent.innerHTML = "<p>Loading rejected recipes...</p>";

    try {
        const res = await fetch(`${API_BASE}/recipes/rejected`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to load rejected recipes");

        const recipes = await res.json();
        displayRejectedRecipes(recipes);

    } catch (err) {
        console.error(err);
        dashboardContent.innerHTML = "<p style='color:red'>Cannot load rejected recipes</p>";
    }
}

// ==================================
// Display Rejected Recipes List
// ==================================
function displayRejectedRecipes(recipes) {
    let html = "<h2>Rejected Recipes</h2>";

    if (!recipes || recipes.length === 0) {
        html += "<p>No rejected recipes</p>";
    } else {
        recipes.forEach(r => {
            const created = new Date(r.createdAt).toLocaleString(); // format date
            html += `
                <div class="recipe-card">
                    <h4>${r.title}</h4>
                    <p><b>Created By:</b> ${r.createdByName}</p>
                    <p><b>Created At:</b> ${created}</p>
                    <button class="btn btn-green" onclick="viewRecipe(${r.id})">
                        View
                    </button>
                </div>
            `;
        });
    }

    dashboardContent.innerHTML = html;
}

// ==================================
// Reject Recipe from Unapproved (optional)
// ==================================
async function rejectRecipe(recipeId) {
    try {
        const res = await fetch(`${API_BASE}/recipes/${recipeId}/reject`, {
            method: "PUT",
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Failed to reject recipe");

        alert("❌ Recipe rejected");
        loadRejected(); // refresh rejected list
        loadUnapproved(); // refresh unapproved list

    } catch (err) {
        console.error(err);
        alert("Rejection failed: " + err.message);
    }
}
document.getElementById("statsBtn")?.addEventListener("click", showStats);
function showStats() {

    const token = localStorage.getItem("token");

    fetch("http://localhost:8081/api/admin/metrics", {
        headers: {
            Authorization: `Bearer ${token}`
        }
    })
    .then(res => {
        if(!res.ok) throw new Error("Unauthorized");
        return res.json();
    })
    .then(data => {

        document.getElementById("dashboard-content").innerHTML = `
            <h2>📊 Dashboard Statistics</h2>

            <div class="stat-card">🍲 Total Recipes : ${data.totalRecipes}</div>

            <div class="stat-card approved">
                ✅ Approved Recipes : ${data.approvedRecipes}
            </div>

            <div class="stat-card pending">
                ⏳ Pending Recipes : ${data.pendingRecipes}
            </div>

            <div class="stat-card rejected">
                ❌ Rejected Recipes : ${data.rejectedRecipes}
            </div>
        `;
    })
    .catch(err => alert("Session expired. Please login again."));
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
        const res = await fetch(`http://localhost:8081/api/comments/recipe/${recipeId}`, {
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

        const loggedUserId = String(localStorage.getItem("userId"));
        const loggedRole = localStorage.getItem("role"); // ADMIN / USER

        comments.forEach(c => {
    const isOwner = String(c.userId) === loggedUserId;
    const isAdmin = loggedRole === "ADMIN";
    const canDelete = isOwner || isAdmin;
    const hasReplies = c.replies && c.replies.length > 0;

    container.innerHTML += `
        <div class="comment-block" id="comment-${c.id}" style="border-bottom:1px solid #8b1d1d; padding:5px;">
            <p><b>${isOwner ? "You" : c.username}</b>: ${c.content}</p>
            <div>
                <button onclick="showReplyForm(${recipeId}, ${c.id})">Reply</button>
                ${canDelete ? `<button onclick="deleteComment(${recipeId}, ${c.id})" style="color:red;">Delete</button>` : ""}
                ${hasReplies ? `<button onclick="toggleReplies(${c.id})">View Replies (${c.replies.length})</button>` : ""}
            </div>
            <div id="replies-${c.id}" style="margin-left:20px; display:none;">
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
        const isOwner = String(r.userId) === loggedUserId;
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
    if (!textarea) return;

    const content = textarea.value.trim();
    if (!content) return alert("Comment cannot be empty");

    fetch(`${COMMENT_API}/${recipeId}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ content })
    })
    .then(res => {
        if (!res.ok) throw new Error("Failed to post comment");
        textarea.value = "";
        loadComments(recipeId);
    })
    .catch(err => alert(err.message));
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
            const res = await fetch(`http://localhost:8081/api/comments/${recipeId}`, {
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

    fetch(`http://localhost:8081/api/comments/${commentId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` }
    })
    .then(() => {
    setTimeout(() => loadComments(recipeId), 300);
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
    



function generateAI(recipeFieldId, ingredientsFieldId, descriptionFieldId) {
    const recipeName = document.getElementById(recipeFieldId).value.trim();
    if (!recipeName) return alert("Enter a recipe name");

    fetch(`/api/recipes/ai-generate?recipeName=${encodeURIComponent(recipeName)}`, {
        method: "POST"
    })
    .then(res => res.json())
    .then(data => {
        // 1️⃣ Fill description
        document.getElementById(descriptionFieldId).value = data.description || "";

        // 2️⃣ Fill ingredients
        if (ingredientsFieldId && Array.isArray(data.ingredients)) {
            const formatted = data.ingredients
                .map(i => `${i.name} ${i.quantity} ${i.unit || "g"}`)
                .join("\n");
            document.getElementById(ingredientsFieldId).value = formatted;
        }

        console.log("AI recipe generated:", data);
    })
    .catch(err => console.error("AI generate error:", err));
}
    