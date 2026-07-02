const API = {
    async request(path, options = {}) {
        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
        };
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(path, {...options, headers});

        if (response.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('userId');
            localStorage.removeItem('email');
            if (!window.location.pathname.includes('login') && !window.location.pathname.includes('register')) {
                window.location.href = '/login.html';
            }
            throw new Error('Unauthorized');
        }

        if (!response.ok) {
            const error = await response.json().catch(() => ({message: 'Request failed'}));
            throw new Error(error.message || 'Request failed');
        }

        if (response.status === 204) {
            return null;
        }

        return response.json();
    },

    register(email, password, repeatPassword) {
        return this.request('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify({email, password, repeatPassword}),
        });
    },

    login(email, password) {
        return this.request('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({email, password}),
        });
    },

    listDocuments() {
        return this.request('/api/documents');
    },

    createDocument() {
        return this.request('/api/documents', {method: 'POST'});
    },

    getDocument(id) {
        return this.request(`/api/documents/${id}`);
    },

    updateDocument(id, title, content) {
        return this.request(`/api/documents/${id}`, {
            method: 'PUT',
            body: JSON.stringify({title, content}),
        });
    },

    deleteDocument(id) {
        return this.request(`/api/documents/${id}`, {method: 'DELETE'});
    },
};
