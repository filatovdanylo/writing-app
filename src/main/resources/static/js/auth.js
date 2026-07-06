const Auth = {
    async isLoggedIn() {
        try {
            await API.me();
            return true;
        } catch {
            return false;
        }
    },

    async requireAuth() {
        const loggedIn = await this.isLoggedIn();
        if (!loggedIn) {
            window.location.href = '/login.html';
        }
    },

    async redirectIfLoggedIn() {
        const loggedIn = await this.isLoggedIn();
        if (loggedIn) {
            window.location.href = '/editor.html';
        }
    },

    async logout() {
        try {
            await API.logout();
        } catch {
            // redirect even if logout request fails
        }
        window.location.href = '/login.html';
    },
};
