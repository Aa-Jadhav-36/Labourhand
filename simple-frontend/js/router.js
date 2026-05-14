/**
 * LabourHand – Router
 * Lightweight auth-guard helper for the simple multi-page frontend.
 * No hash routing needed — each page is a separate .html file.
 *
 * Usage (at top of every protected page):
 *   Router.requireAuth();           // redirects to login.html if not logged in
 *   Router.requireRole('worker');   // also checks role
 */

const Router = {

    // Redirect to login if no valid session exists
    requireAuth(redirectAfter = location.href) {
        if (!window.SessionManager?.isLoggedIn()) {
            location.href = `login.html?redirect=${encodeURIComponent(redirectAfter)}`;
        }
    },

    // Redirect if session exists but role doesn't match
    requireRole(role) {
        Router.requireAuth();
        const user = window.SessionManager?.getCurrentUser();
        if (!user) return;
        const userRole = user.role; // 'worker' | 'owner'
        if (userRole !== role) {
            // Send owners to contractor, workers to dashboard
            location.href = userRole === 'owner' ? 'contractor.html' : 'dashboard.html';
        }
    },

    // Redirect logged-in users away from public pages (landing, login, register)
    redirectIfLoggedIn() {
        const user = window.SessionManager?.getCurrentUser();
        if (user) {
            location.href = user.role === 'owner' ? 'contractor.html' : 'dashboard.html';
        }
    },

    // Navigate to a page, optionally attaching query params
    go(page, params = {}) {
        const qs = new URLSearchParams(params).toString();
        location.href = qs ? `${page}?${qs}` : page;
    },

    // Read a query param by name from the current URL
    param(name) {
        return new URLSearchParams(location.search).get(name);
    },

    // Logout helper
    logout() {
        window.SessionManager?.clearSession();
        location.href = 'index.html';
    },
};

window.Router = Router;
