// ================= BASE API CONFIG =================
const API_BASE_URL = "http://localhost:8081/api";

// ================= TOKEN HELPERS =================
function setToken(token) {
    localStorage.setItem("token", token);
}

function getToken() {
    return localStorage.getItem("token");
}

function getRole() {
    return localStorage.getItem("role");
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "login.html";
}

// ================= API REQUEST (FETCH WRAPPER) =================
async function apiRequest(endpoint, method = "GET", body = null) {
    const headers = { "Content-Type": "application/json" };

    const token = getToken();
    if (token) {
        headers.Authorization = "Bearer " + token;
    }

    const response = await fetch(API_BASE_URL + endpoint, {
        method,
        headers,
        body: body ? JSON.stringify(body) : null
    });

    if (response.status === 401) {
        alert("Session expired. Please login again.");
        logout();
        return null;
    }

    if (response.status === 403) {
        alert("Access denied.");
        return null;
    }

    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `HTTP error ${response.status}`);
    }

    const text = await response.text();
    if (!text) return null;

    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}
