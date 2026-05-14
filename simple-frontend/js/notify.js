/**
 * LabourHand – Notify
 * Replaces default browser alert() and confirm() with beautiful Bootstrap modals.
 */

const Notify = {
    _modal: null,
    _bsModal: null,

    /**
     * Show a beautiful alert
     * @param {string} message 
     * @param {string} title 
     * @param {string} type 'primary', 'success', 'danger', 'warning'
     */
    alert(message, title = 'Notification', type = 'primary') {
        this._ensureModal();
        const header = document.getElementById('notify-header');
        const body = document.getElementById('notify-body');
        const footer = document.getElementById('notify-footer');
        const titleEl = document.getElementById('notify-title');

        titleEl.textContent = title;
        header.className = `modal-header bg-${type} text-white`;
        body.innerHTML = `<p class="mb-0 fs-5">${message}</p>`;
        footer.innerHTML = `<button type="button" class="btn btn-${type} fw-bold px-4" data-bs-dismiss="modal">OK</button>`;

        this._bsModal.show();
    },

    /**
     * Show a beautiful confirmation
     * @param {string} message 
     * @param {Function} onConfirm Callback when user clicks 'Yes'
     * @param {string} title 
     */
    confirm(message, onConfirm, title = 'Confirm Action') {
        this._ensureModal();
        const header = document.getElementById('notify-header');
        const body = document.getElementById('notify-body');
        const footer = document.getElementById('notify-footer');
        const titleEl = document.getElementById('notify-title');

        titleEl.textContent = title;
        header.className = `modal-header bg-dark text-white`;
        body.innerHTML = `<p class="mb-0 fs-5">${message}</p>`;
        
        footer.innerHTML = `
            <button type="button" class="btn btn-light fw-bold" data-bs-dismiss="modal">Cancel</button>
            <button type="button" class="btn btn-primary fw-bold px-4" id="notify-confirm-btn">Yes, Proceed</button>
        `;

        const btn = document.getElementById('notify-confirm-btn');
        btn.onclick = () => {
            this._bsModal.hide();
            if (onConfirm) onConfirm();
        };

        this._bsModal.show();
    },

    /**
     * Success toast
     */
    success(message) {
        // Fallback to alert for now, or implement toast
        this.alert(message, 'Success', 'success');
    },

    /**
     * Error toast
     */
    error(message) {
        this.alert(message, 'Error', 'danger');
    },

    /**
     * Show a beautiful prompt
     * @param {string} message 
     * @param {string} defaultValue 
     * @param {Function} onConfirm Callback with the input value
     */
    prompt(message, defaultValue = '', onConfirm) {
        this._ensureModal();
        const header = document.getElementById('notify-header');
        const body = document.getElementById('notify-body');
        const footer = document.getElementById('notify-footer');
        const titleEl = document.getElementById('notify-title');

        titleEl.textContent = 'Input Required';
        header.className = `modal-header bg-primary text-white`;
        body.innerHTML = `
            <p class="mb-3 text-start">${message}</p>
            <input type="text" id="notify-input" class="form-control form-control-lg" value="${defaultValue}" />
        `;
        
        footer.innerHTML = `
            <button type="button" class="btn btn-light fw-bold" data-bs-dismiss="modal">Cancel</button>
            <button type="button" class="btn btn-primary fw-bold px-4" id="notify-confirm-btn">Confirm</button>
        `;

        const input = document.getElementById('notify-input');
        setTimeout(() => input.focus(), 500);

        const btn = document.getElementById('notify-confirm-btn');
        btn.onclick = () => {
            const val = input.value.trim();
            this._bsModal.hide();
            if (onConfirm) onConfirm(val);
        };

        this._bsModal.show();
    },

    _ensureModal() {
        if (this._modal) return;

        const html = `
            <div class="modal fade" id="lh-notify-modal" tabindex="-1" style="z-index: 9999;">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content border-0 shadow-lg overflow-hidden">
                        <div id="notify-header" class="modal-header">
                            <h5 class="modal-title fw-bold" id="notify-title">Notification</h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body p-4 text-center" id="notify-body">
                            <!-- JS Content -->
                        </div>
                        <div class="modal-footer border-0 justify-content-center pb-4" id="notify-footer">
                            <!-- JS Content -->
                        </div>
                    </div>
                </div>
            </div>
        `;
        const div = document.createElement('div');
        div.innerHTML = html;
        document.body.appendChild(div);

        this._modal = document.getElementById('lh-notify-modal');
        this._bsModal = new bootstrap.Modal(this._modal);
    }
};

window.Notify = Notify;
