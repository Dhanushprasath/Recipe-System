// ================= API REQUEST HELPER =================
async function apiRequest(url, method = "GET", data = null) {
    const headers = { "Content-Type": "application/json" };
    const token = localStorage.getItem("token");
    if (token) headers["Authorization"] = `Bearer ${token}`;

    const options = { method, headers };
    if (data) options.body = JSON.stringify(data);

    const response = await fetch(url, options);
    const resData = await response.json();

    if (!response.ok) throw new Error(resData?.message || "Request failed");
    return resData;
}


// ================= REGISTER =================
async function register() {
    const username = document.getElementById("name").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const role = document.getElementById("role").value;

    if (!username || !email || !password || !confirmPassword) {
        alert("All fields are required!");
        return;
    }
    if (password !== confirmPassword) {
        alert("Passwords do not match!");
        return;
    }

    try {
        const res = await apiRequest("/auth/register", "POST", { username, email, password, role });
        alert(res?.message || "Registration successful");
        window.location.href = "login.html";
    } catch (err) {
        alert(err.message || "Registration failed");
    }
}


// ================= LOGIN =================
async function login() {
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();

    if (!email || !password) {
        alert("Please enter both email and password");
        return;
    }

    try {
        const res = await apiRequest("/auth/login", "POST", { email, password });

        if (!res?.token || !res?.role) {
            alert("Invalid email or password");
            return;
        }

        // Save token & role
        localStorage.setItem("token", res.token);
        localStorage.setItem("role", res.role);

        // ROLE BASED REDIRECT
        if (res.role.toUpperCase() === "ADMIN") {
            window.location.replace("admin-dashboard.html");
        } else {
            window.location.replace("dashboard.html");
        }

    } catch (err) {
        alert(err.message || "Login failed. Check your credentials.");
    }
}


// ================= FORGOT PASSWORD =================
async function forgotPassword() {
    const email = document.getElementById("email").value.trim();
    if (!email) {
        alert("Please enter email");
        return;
    }

    try {
        const res = await apiRequest("/auth/forgot-password", "POST", { email });
        alert(res?.message || "OTP sent to your email");
        document.getElementById("otp-section").style.display = "block";
    } catch (err) {
        alert(err.message || "Failed to send OTP");
    }
}


// ================= VERIFY RESET OTP =================
async function verifyResetOtp() {
    const email = document.getElementById("email").value.trim();
    const otp = document.getElementById("otp").value.trim();
    if (!otp) {
        alert("Enter OTP");
        return;
    }

    try {
        const res = await apiRequest("/auth/verify-reset-otp", "POST", { email, otp });
        alert(res?.message || "OTP verified successfully");
        document.getElementById("reset-section").style.display = "block";
    } catch (err) {
        alert(err.message || "OTP verification failed");
    }
}


// ================= RESET PASSWORD =================
async function resetPassword() {
    const email = document.getElementById("email").value.trim();
    const newPassword = document.getElementById("newPassword").value;
    if (!newPassword) {
        alert("Enter new password");
        return;
    }

    try {
        const res = await apiRequest("/auth/reset-password", "POST", { email, newPassword });
        alert(res?.message || "Password reset successful");
        window.location.href = "login.html";
    } catch (err) {
        alert(err.message || "Password reset failed");
    }
}
