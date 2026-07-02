const Auth = {
    saveSession(response) {
        localStorage.setItem('token', response.token);
        localStorage.setItem('userId', response.userId);
        localStorage.setItem('email', response.email);
    },

    clearSession() {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('email');
    },

    isLoggedIn() {
        return !!localStorage.getItem('token');
    },

    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = '/login.html';
        }
    },

    redirectIfLoggedIn() {
        if (this.isLoggedIn()) {
            window.location.href = '/editor.html';
        }
    },

    logout() {
        this.clearSession();
        window.location.href = '/login.html';
    },
};
