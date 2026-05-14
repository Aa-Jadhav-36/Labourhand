/**
 * LabourHand – API Service
 * Mirrors every endpoint group from src/utils/api.ts
 * All methods return Promises. JWT is auto-attached from SessionManager.
 *
 * Usage:
 *   const projects = await Api.project.getNearby(12.9716, 77.5946, 10);
 */

const BASE_URL = 'http://localhost:8081/api';

// ─── Core fetch wrapper ───────────────────────────────────────────────────────

async function _request(method, path, body) {
    const token = window.SessionManager?.getToken() ?? null;

    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(`${BASE_URL}${path}`, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error(msg || `HTTP ${res.status}`);
    }

    if (res.status === 204) return undefined;
    return res.json();
}

// ─── Auth ─────────────────────────────────────────────────────────────────────
const authApi = {
    login: (email, password) => _request('POST', '/auth/login', { email, password }),
    register: (data) => _request('POST', '/auth/register', data),
};

// ─── Users ────────────────────────────────────────────────────────────────────
const userApi = {
    getMe: () => _request('GET', '/users/me'),
    updateMe: (data) => _request('PUT', '/users/me', data),
    getAll: () => _request('GET', '/users'),
    getById: (id) => _request('GET', `/users/${id}`),
};

// ─── Projects ─────────────────────────────────────────────────────────────────
const projectApi = {
    getAll: () => _request('GET', '/projects'),
    getNearby: (lat = 12.9716, lng = 77.5946, radius = 10) =>
        _request('GET', `/projects/nearby?lat=${lat}&lng=${lng}&radius=${radius}`),
    getByCategory: (cat) => _request('GET', `/projects/category/${cat}`),
    getNearbyByCategory: (lat, lng, radius, cat) =>
        _request('GET', `/projects/nearby/category/${cat}?lat=${lat}&lng=${lng}&radius=${radius}`),
    getMyProjects: () => _request('GET', '/projects/my'),
    getById: (id) => _request('GET', `/projects/${id}`),
    create: (data) => _request('POST', '/projects', data),
    update: (id, data) => _request('PUT', `/projects/${id}`, data),
    updateProgress: (id, progress, status) => _request('PUT', `/projects/${id}/progress`, { progress, status }),
    getUpdates: (id) => _request('GET', `/projects/${id}/updates`),
    postUpdate: (id, data) => _request('POST', `/projects/${id}/updates`, data),
    payUpdate: (id, data) => _request('POST', `/projects/${id}/pay`, data),
    getPayments: (id) => _request('GET', `/projects/${id}/payments`),
    delete: (id) => _request('DELETE', `/projects/${id}`),
};

// ─── Bids ─────────────────────────────────────────────────────────────────────
const bidApi = {
    getProjectBids: (projectId) => _request('GET', `/projects/${projectId}/bids`),
    getMyBids: () => _request('GET', '/bids/my'),
    getById: (id) => _request('GET', `/bids/${id}`),
    place: (data) => _request('POST', '/bids', data),
    update: (id, data) => _request('PUT', `/bids/${id}`, data),
    withdraw: (id) => _request('DELETE', `/bids/${id}`),
    accept: (id) => _request('PUT', `/bids/${id}/accept`),
    reject: (id) => _request('PUT', `/bids/${id}/reject`),
};

// ─── Workers ──────────────────────────────────────────────────────────────────
const workerApi = {
    getAll: () => _request('GET', '/workers'),
    getById: (id) => _request('GET', `/workers/${id}`),
    getReviews: (id) => _request('GET', `/workers/${id}/reviews`),
    getRatings: (id) => _request('GET', `/workers/${id}/ratings`),
    getCertifications: (id) => _request('GET', `/workers/${id}/certifications`),
    addCertification: (data) => _request('POST', '/workers/certifications', data),
    deleteCertification: (id) => _request('DELETE', `/workers/certifications/${id}`),
    updatePaymentDetails: (data) => _request('POST', '/workers/payment-details', data),
};

// ─── Reviews ──────────────────────────────────────────────────────────────────
const reviewApi = {
    getAll: () => _request('GET', '/reviews'),
    create: (data) => _request('POST', '/reviews', data),
    delete: (id) => _request('DELETE', `/reviews/${id}`),
};

// ─── Messages ─────────────────────────────────────────────────────────────────
const messageApi = {
    getConversations: () => _request('GET', '/messages'),
    getMessages: (conversationId) => _request('GET', `/messages/${conversationId}`),
    send: (receiverId, content, projectId) => _request('POST', '/messages', { receiverId, content, projectId }),
    delete: (messageId) => _request('DELETE', `/messages/${messageId}`),
};

// ─── Skills ───────────────────────────────────────────────────────────────────
const skillApi = {
    getAll: () => _request('GET', '/skills'),
    create: (name) => _request('POST', '/skills', { name }),
};

// ─── Contractor ───────────────────────────────────────────────────────────────
const contractorApi = {
    getTeam: () => _request('GET', '/contractor/team'),
    getProjects: () => _request('GET', '/contractor/projects'),
    getEarnings: (period = 'week') => _request('GET', `/contractor/earnings?period=${period}`),
    getSchedule: () => _request('GET', '/contractor/schedule'),
    createScheduleEvent: (data) => _request('POST', '/contractor/schedule', data),
    deleteScheduleEvent: (id) => _request('DELETE', `/contractor/schedule/${id}`),
};

// ─── Global Export ────────────────────────────────────────────────────────────
window.Api = {
    auth: authApi,
    user: userApi,
    project: projectApi,
    bid: bidApi,
    worker: workerApi,
    review: reviewApi,
    message: messageApi,
    skill: skillApi,
    contractor: contractorApi,

    // Expose the raw _request for one-offs
    raw: _request,
};
