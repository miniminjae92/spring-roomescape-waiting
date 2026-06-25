document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("login-form");
    const signupForm = document.getElementById("signup-form");
    const message = document.getElementById("auth-message");
    const params = new URLSearchParams(window.location.search);
    const next = params.get("next") || "/";

    function showMessage(text, error = false) {
        message.textContent = text;
        message.className = error ? "auth-message error" : "auth-message success";
    }

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = new FormData(loginForm);
        try {
            const member = await RoomescapeApi.login({
                loginId: form.get("loginId"),
                password: form.get("password")
            });
            window.location.href = member.role === "MANAGER" && next === "/" ? "/admin" : next;
        } catch (error) {
            showMessage(error.message, true);
        }
    });

    signupForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = new FormData(signupForm);
        try {
            await RoomescapeApi.signup({
                loginId: form.get("loginId"),
                password: form.get("password"),
                name: form.get("name")
            });
            window.location.href = next;
        } catch (error) {
            showMessage(error.message, true);
        }
    });
});
