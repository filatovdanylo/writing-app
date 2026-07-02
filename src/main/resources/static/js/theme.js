const Theme = {
    init() {
        const saved = localStorage.getItem('theme') || 'light';
        document.documentElement.setAttribute('data-theme', saved);
        this.updateToggleIcon();
    },

    toggle() {
        const current = document.documentElement.getAttribute('data-theme') || 'light';
        const next = current === 'light' ? 'dark' : 'light';
        document.documentElement.setAttribute('data-theme', next);
        localStorage.setItem('theme', next);
        this.updateToggleIcon();
    },

    updateToggleIcon() {
        const btn = document.getElementById('theme-toggle');
        if (!btn) return;
        const theme = document.documentElement.getAttribute('data-theme');
        btn.textContent = theme === 'dark' ? '☀' : '☾';
        btn.title = theme === 'dark' ? 'Light mode' : 'Dark mode';
    },
};

document.addEventListener('DOMContentLoaded', () => Theme.init());
