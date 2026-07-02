const Documents = {
    list: [],
    currentId: null,

    async loadList() {
        this.list = await API.listDocuments();
        this.renderList();
        return this.list;
    },

    renderList() {
        const ul = document.getElementById('document-list');
        if (!ul) return;
        ul.innerHTML = '';

        this.list.forEach((doc) => {
            const li = document.createElement('li');
            li.className = 'document-item' + (doc.id === this.currentId ? ' active' : '');
            li.dataset.id = doc.id;

            const info = document.createElement('div');
            info.className = 'document-item-info';

            const title = document.createElement('div');
            title.className = 'document-item-title';
            title.textContent = doc.title || 'Untitled';

            const date = document.createElement('div');
            date.className = 'document-item-date';
            date.textContent = this.formatDate(doc.updatedAt);

            info.appendChild(title);
            info.appendChild(date);

            const delBtn = document.createElement('button');
            delBtn.className = 'document-item-delete';
            delBtn.textContent = 'Delete';
            delBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                Editor.deleteDocument(doc.id);
            });

            li.appendChild(info);
            li.appendChild(delBtn);
            li.addEventListener('click', () => Editor.switchDocument(doc.id));
            ul.appendChild(li);
        });
    },

    formatDate(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        return d.toLocaleDateString(undefined, {month: 'short', day: 'numeric', year: 'numeric'});
    },

    updateListItem(id, title) {
        const doc = this.list.find((d) => d.id === id);
        if (doc) {
            doc.title = title;
            this.renderList();
        }
    },
};
