const Editor = {
    currentId: null,
    saveTimer: null,
    isSaving: false,
    chromeVisible: false,
    drawerOpen: false,
    dirty: false,

    async init() {
        Auth.requireAuth();

        const docs = await Documents.loadList();
        if (docs.length === 0) {
            const doc = await API.createDocument();
            await this.loadDocument(doc.id);
        } else {
            await this.loadDocument(docs[0].id);
        }

        this.bindEvents();
        this.bindChrome();
        this.bindShortcuts();
    },

    bindEvents() {
        const textarea = document.getElementById('editor-textarea');
        const titleInput = document.getElementById('title-input');

        textarea.addEventListener('input', () => {
            this.dirty = true;
            this.resizeTextarea();
            this.scheduleSave();
        });

        titleInput.addEventListener('input', () => {
            this.dirty = true;
            this.scheduleSave();
        });

        document.getElementById('btn-documents').addEventListener('click', () => this.toggleDrawer());
        document.getElementById('btn-new-doc').addEventListener('click', () => this.createDocument());
        document.getElementById('btn-logout').addEventListener('click', () => Auth.logout());
        document.getElementById('theme-toggle').addEventListener('click', () => Theme.toggle());
        document.getElementById('drawer-close').addEventListener('click', () => this.closeDrawer());
        document.getElementById('drawer-overlay').addEventListener('click', () => this.closeDrawer());
        window.addEventListener('resize', () => this.resizeTextarea());
    },

    bindChrome() {
        const trigger = document.getElementById('chrome-trigger');
        const chrome = document.getElementById('chrome');

        trigger.addEventListener('mouseenter', () => this.showChrome());
        chrome.addEventListener('mouseleave', () => this.hideChrome());

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                if (this.drawerOpen) {
                    this.closeDrawer();
                } else {
                    this.toggleChrome();
                }
            }
        });
    },

    bindShortcuts() {
        document.addEventListener('keydown', (e) => {
            if (!(e.ctrlKey || e.metaKey)) return;

            if (e.key === 's') {
                e.preventDefault();
                this.saveNow();
            } else if (e.key === 'n') {
                e.preventDefault();
                this.createDocument();
            } else if (e.key === 'o') {
                e.preventDefault();
                this.toggleDrawer();
            }
        });
    },

    showChrome() {
        document.getElementById('chrome').classList.add('visible');
        this.chromeVisible = true;
    },

    hideChrome() {
        document.getElementById('chrome').classList.remove('visible');
        this.chromeVisible = false;
    },

    toggleChrome() {
        if (this.chromeVisible) {
            this.hideChrome();
        } else {
            this.showChrome();
        }
    },

    toggleDrawer() {
        if (this.drawerOpen) {
            this.closeDrawer();
        } else {
            this.openDrawer();
        }
    },

    openDrawer() {
        this.drawerOpen = true;
        document.getElementById('drawer').classList.add('open');
        document.getElementById('drawer-overlay').classList.add('open');
        Documents.loadList();
    },

    closeDrawer() {
        this.drawerOpen = false;
        document.getElementById('drawer').classList.remove('open');
        document.getElementById('drawer-overlay').classList.remove('open');
    },

    async loadDocument(id) {
        const doc = await API.getDocument(id);
        this.currentId = doc.id;
        Documents.currentId = doc.id;

        document.getElementById('title-input').value = doc.title || 'Untitled';
        document.getElementById('editor-textarea').value = doc.content || '';
        this.dirty = false;
        this.resizeTextarea();

        Documents.renderList();
        this.closeDrawer();
        document.getElementById('editor-textarea').focus();
    },

    async switchDocument(id) {
        if (id === this.currentId) {
            this.closeDrawer();
            return;
        }
        if (this.dirty) {
            await this.saveNow();
        }
        await this.loadDocument(id);
    },

    async createDocument() {
        if (this.dirty) {
            await this.saveNow();
        }
        const doc = await API.createDocument();
        await Documents.loadList();
        await this.loadDocument(doc.id);
    },

    async deleteDocument(id) {
        if (!confirm('Delete this document?')) return;

        await API.deleteDocument(id);
        await Documents.loadList();

        if (id === this.currentId) {
            if (Documents.list.length > 0) {
                await this.loadDocument(Documents.list[0].id);
            } else {
                const doc = await API.createDocument();
                await Documents.loadList();
                await this.loadDocument(doc.id);
            }
        } else {
            Documents.renderList();
        }
    },

    resizeTextarea() {
        const textarea = document.getElementById('editor-textarea');
        textarea.style.height = 'auto';
        textarea.style.height = `${textarea.scrollHeight}px`;
    },

    scheduleSave() {
        clearTimeout(this.saveTimer);
        this.saveTimer = setTimeout(() => this.saveNow(), 1500);
    },

    async saveNow() {
        if (!this.currentId || this.isSaving) return;

        const title = document.getElementById('title-input').value.trim() || 'Untitled';
        const content = document.getElementById('editor-textarea').value;

        this.isSaving = true;
        this.setSaveStatus('Saving…');

        try {
            const updated = await API.updateDocument(this.currentId, title, content);
            this.dirty = false;
            Documents.updateListItem(this.currentId, updated.title);
            this.setSaveStatus('Saved');
            setTimeout(() => this.setSaveStatus('', true), 2000);
        } catch (err) {
            this.setSaveStatus('Error saving');
        } finally {
            this.isSaving = false;
        }
    },

    setSaveStatus(text, hide) {
        const el = document.getElementById('save-status');
        el.textContent = text;
        if (hide) {
            el.classList.add('hidden');
        } else {
            el.classList.remove('hidden');
        }
    },
};

document.addEventListener('DOMContentLoaded', () => Editor.init());
