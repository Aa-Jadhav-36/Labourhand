/**
 * LabourHand – DataMapper
 * Generic DOM renderers: injects API JSON into Bootstrap tables / card lists.
 *
 * Usage:
 *   DataMapper.toTable('#my-table', data, [
 *     { label: 'Title',    key: 'title' },
 *     { label: 'Status',   key: 'status', render: (v) => DataMapper.statusBadge(v) },
 *     { label: 'Budget',   key: 'budget' },
 *   ]);
 *
 *   DataMapper.toCards('#my-list', data, (item) => `
 *     <div class="card mb-2">...</div>
 *   `);
 */

const DataMapper = {

    // ─── Table renderer ────────────────────────────────────────────────────────
    /**
     * @param {string|Element} container  CSS selector or DOM element
     * @param {Object[]}       data       Array of plain objects
     * @param {Object[]}       columns    [ { label, key, render? } ]
     */
    toTable(container, data, columns) {
        const el = typeof container === 'string' ? document.querySelector(container) : container;
        if (!el) { console.warn('DataMapper.toTable: container not found', container); return; }

        if (!data || data.length === 0) {
            el.innerHTML = `<p class="text-muted text-center py-4">No data available.</p>`;
            return;
        }

        const thead = columns.map(c => `<th scope="col">${c.label}</th>`).join('');
        const tbody = data.map(row => {
            const cells = columns.map(c => {
                const raw = DataMapper._get(row, c.key);
                const display = c.render ? c.render(raw, row) : (raw ?? '—');
                return `<td>${display}</td>`;
            }).join('');
            const clickAttr = row.id ? `data-id="${row.id}"` : '';
            return `<tr class="align-middle" ${clickAttr} style="${row.id ? 'cursor:pointer' : ''}">${cells}</tr>`;
        }).join('');

        el.innerHTML = `
      <div class="table-responsive">
        <table class="table table-hover table-bordered align-middle mb-0">
          <thead class="table-light"><tr>${thead}</tr></thead>
          <tbody>${tbody}</tbody>
        </table>
      </div>`;
    },

    // ─── Card-list renderer ────────────────────────────────────────────────────
    /**
     * @param {string|Element} container     CSS selector or DOM element
     * @param {Object[]}       data          Array of plain objects
     * @param {Function}       cardTemplate  (item) => HTML string
     */
    toCards(container, data, cardTemplate) {
        const el = typeof container === 'string' ? document.querySelector(container) : container;
        if (!el) { console.warn('DataMapper.toCards: container not found', container); return; }

        if (!data || data.length === 0) {
            el.innerHTML = `<p class="text-muted text-center py-4">No items found.</p>`;
            return;
        }

        el.innerHTML = data.map(cardTemplate).join('');
    },

    // ─── Spinner helpers ───────────────────────────────────────────────────────
    showSpinner(container) {
        const el = typeof container === 'string' ? document.querySelector(container) : container;
        if (!el) return;
        el.innerHTML = `
      <div class="d-flex justify-content-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading…</span>
        </div>
      </div>`;
    },

    showError(container, message = 'Something went wrong.') {
        const el = typeof container === 'string' ? document.querySelector(container) : container;
        if (!el) return;
        el.innerHTML = `<div class="alert alert-danger">${message}</div>`;
    },

    // ─── Status badge ──────────────────────────────────────────────────────────
    statusBadge(status) {
        const map = {
            OPEN_FOR_BIDS: ['bg-success', 'Open for Bids'],
            IN_PROGRESS: ['bg-primary', 'In Progress'],
            COMPLETED: ['bg-secondary', 'Completed'],
            PAYMENT_VERIFIED: ['bg-success', 'Payment Verified'],
            PENDING: ['bg-warning text-dark', 'Pending'],
            ACCEPTED: ['bg-success', 'Accepted'],
            REJECTED: ['bg-danger', 'Rejected'],
        };
        const [cls, label] = map[status] || ['bg-secondary', status || 'Unknown'];
        return `<span class="badge ${cls}">${label}</span>`;
    },

    // ─── Rating stars ──────────────────────────────────────────────────────────
    starRating(rating) {
        const full = Math.floor(rating);
        const half = rating - full >= 0.5;
        const empty = 5 - full - (half ? 1 : 0);
        return (
            '★'.repeat(full) +
            (half ? '½' : '') +
            '☆'.repeat(empty) +
            ` <small class="text-muted">(${rating})</small>`
        );
    },

    // ─── Progress bar ──────────────────────────────────────────────────────────
    progressBar(value, label = '') {
        const pct = Math.min(100, Math.max(0, value));
        return `
      <div class="progress" style="height: 8px;" title="${label || pct + '%'}">
        <div class="progress-bar bg-primary" role="progressbar"
             style="width:${pct}%" aria-valuenow="${pct}" aria-valuemin="0" aria-valuemax="100">
        </div>
      </div>
      <small class="text-muted">${pct}%</small>`;
    },

    // ─── Currency formatter ────────────────────────────────────────────────────
    currency(amount) {
        if (amount === null || amount === undefined) return '—';
        return `₹${Number(amount).toLocaleString('en-IN')}`;
    },

    // ─── Avatar renderer ───────────────────────────────────────────────────────
    avatar(url, name = 'User', size = 40) {
        const fallback = `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=random&color=fff&size=${size}`;
        const finalUrl = url && url.startsWith('http') ? url : fallback;
        return `<img src="${finalUrl}" class="rounded-circle border" 
                     style="width:${size}px; height:${size}px; object-fit:cover;" 
                     alt="${name}" title="${name}" />`;
    },

    // ─── Relative time ─────────────────────────────────────────────────────────
    timeAgo(isoString) {
        if (!isoString) return '—';
        const diff = Math.floor((Date.now() - new Date(isoString)) / 1000);
        if (diff < 60) return 'just now';
        if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
        if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
        return `${Math.floor(diff / 86400)}d ago`;
    },

    // ─── Private helpers ───────────────────────────────────────────────────────
    _get(obj, key) {
        return key.split('.').reduce((o, k) => (o != null ? o[k] : undefined), obj);
    },
};

window.DataMapper = DataMapper;
