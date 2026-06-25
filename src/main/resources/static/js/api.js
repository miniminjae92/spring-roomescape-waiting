window.RoomescapeApi = (() => {
    async function request(url, options = {}) {
        const response = await fetch(url, {
            credentials: "same-origin",
            ...options,
            headers: {
                Accept: "application/json",
                ...(options.body ? {"Content-Type": "application/json"} : {}),
                ...(options.headers || {})
            }
        });
        if (response.status === 204) {
            return null;
        }
        const body = await response.json().catch(() => ({}));
        if (!response.ok) {
            const error = new Error(body.message || "요청 처리에 실패했습니다.");
            error.code = body.code;
            error.action = body.action;
            error.status = response.status;
            throw error;
        }
        return body;
    }

    return {
        login: (payload) => request("/auth/login", {
            method: "POST",
            body: JSON.stringify(payload)
        }),
        signup: (payload) => request("/auth/signup", {
            method: "POST",
            body: JSON.stringify(payload)
        }),
        me: () => request("/auth/me"),
        logout: () => request("/auth/logout", {method: "POST"}),
        request
    };
})();
