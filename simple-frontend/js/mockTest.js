/**
 * LabourHand – Mock Test Script
 * Paste-and-run console diagnostic: pings every Spring Boot endpoint in parallel.
 *
 * Usage (in browser DevTools console, while on any page):
 *   MockTest.runAll()          → runs all tests, prints colour-coded report
 *   MockTest.ping('/projects') → pings a single endpoint
 *
 * The script auto-detects the JWT token from localStorage so protected endpoints
 * are tested with real auth.
 */

const MockTest = (() => {

    const BASE = 'http://localhost:8081/api';

    function _token() {
        try {
            return JSON.parse(localStorage.getItem('labourhand_session'))?.token ?? null;
        } catch { return null; }
    }

    async function ping(path, method = 'GET', body) {
        const token = _token();
        const headers = { 'Content-Type': 'application/json' };
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const start = performance.now();
        try {
            const res = await fetch(`${BASE}${path}`, {
                method,
                headers,
                body: body ? JSON.stringify(body) : undefined,
            });
            const ms = Math.round(performance.now() - start);
            return { path, method, status: res.status, ok: res.ok, ms };
        } catch (err) {
            const ms = Math.round(performance.now() - start);
            return { path, method, status: 0, ok: false, ms, error: err.message };
        }
    }

    // All endpoints to test
    const ALL_ENDPOINTS = [
        // Auth (public)
        { path: '/auth/login', method: 'POST', body: { email: 'rajesh@labourhand.com', password: 'worker123' } },

        // Users
        { path: '/users/me' },
        { path: '/users' },

        // Projects
        { path: '/projects' },
        { path: '/projects/nearby?lat=12.9716&lng=77.5946&radius=10' },
        { path: '/projects/my' },

        // Bids
        { path: '/bids/my' },

        // Workers
        { path: '/workers' },

        // Reviews
        { path: '/reviews' },

        // Messages
        { path: '/messages' },

        // Skills
        { path: '/skills' },

        // Contractor
        { path: '/contractor/team' },
        { path: '/contractor/projects' },
        { path: '/contractor/earnings?period=week' },
        { path: '/contractor/schedule' },
    ];

    async function runAll() {
        const token = _token();
        console.group('%c🔍 LabourHand API Connectivity Test', 'font-size:14px;font-weight:bold;color:#f97316');
        console.log(`Base URL : ${BASE}`);
        console.log(`Auth     : ${token ? '✅ Token found' : '⚠️  No token — protected endpoints may fail'}`);
        console.log(`Running ${ALL_ENDPOINTS.length} tests in parallel…\n`);

        const results = await Promise.all(ALL_ENDPOINTS.map(e => ping(e.path, e.method, e.body)));

        let passed = 0, failed = 0;
        results.forEach(r => {
            const icon = r.ok ? '✅' : '❌';
            const style = r.ok ? 'color:#16a34a' : 'color:#dc2626';
            const info = r.error ? ` (${r.error})` : '';
            console.log(
                `%c${icon} [${r.method.padEnd(6)}] ${r.path.padEnd(50)} → HTTP ${r.status || 'ERR'} (${r.ms}ms)${info}`,
                style
            );
            r.ok ? passed++ : failed++;
        });

        console.log('');
        console.log(
            `%cResult: ${passed} passed, ${failed} failed`,
            passed === results.length ? 'font-weight:bold;color:#16a34a' : 'font-weight:bold;color:#dc2626'
        );
        console.groupEnd();
        return results;
    }

    return { runAll, ping };
})();

window.MockTest = MockTest;

// Auto-hint in console
console.log('%c[LabourHand] MockTest loaded. Run: MockTest.runAll()', 'color:#f97316;font-weight:bold');
