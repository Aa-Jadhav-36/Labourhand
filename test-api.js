const BASE_URL = 'http://127.0.0.1:8081/api';

async function req(method, path, body, token) {
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const res = await fetch(`${BASE_URL}${path}`, {
        method, headers, body: body ? JSON.stringify(body) : undefined
    });
    const text = await res.text();
    if (!res.ok) throw new Error(`HTTP ${res.status}: ${text}`);
    return text ? JSON.parse(text) : null;
}

async function runTests() {
    console.log("=== STARTING API TESTS ===");
    try {
        const unique = Math.floor(Math.random() * 10000);
        
        // 1. REGISTER OWNER
        console.log(`\n1. Registering Owner (owner${unique}@test.com)...`);
        const ownerRes = await req('POST', '/auth/register', {
            name: "Test Owner", email: `owner${unique}@test.com`, phone: "111", password: "password123",
            role: "OWNER", companyName: "Test Co"
        });
        const ownerToken = ownerRes.token;
        console.log("✅ Owner registered successfully. Token length:", ownerToken.length);

        // 2. OWNER CREATES PROJECT
        console.log("\n2. Owner creating a project...");
        const projectRes = await req('POST', '/projects', {
            title: "Test Project", description: "This is a test project", category: "Masonry",
            budget: 5000, timelineDays: 10, location: "Bangalore", lat: 12.0, lng: 77.0
        }, ownerToken);
        const projectId = projectRes.id;
        console.log("✅ Project created successfully. ID:", projectId);

        // 3. REGISTER WORKER
        console.log(`\n3. Registering Worker (worker${unique}@test.com)...`);
        const workerRes = await req('POST', '/auth/register', {
            name: "Test Worker", email: `worker${unique}@test.com`, phone: "222", password: "password123",
            role: "WORKER", specialization: "Mason"
        });
        const workerToken = workerRes.token;
        console.log("✅ Worker registered successfully. Token length:", workerToken.length);

        // 4. WORKER FETCHES NEARBY PROJECTS
        console.log("\n4. Worker fetching nearby projects...");
        const nearby = await req('GET', '/projects/nearby?lat=12.0&lng=77.0&radius=50', null, workerToken);
        const found = nearby.find(p => p.id === projectId);
        console.log(`✅ Nearby projects fetched (${nearby.length} total). Found newly created project:`, !!found);

        // 5. WORKER PLACES BID
        console.log("\n5. Worker placing bid on project...");
        const bidRes = await req('POST', '/bids', {
            projectId: projectId, amount: 4500, estimatedDays: 8, message: "I can do this!"
        }, workerToken);
        const bidId = bidRes.id;
        console.log("✅ Bid placed successfully. ID:", bidId);

        // 6. WORKER MODIFIES BID
        console.log("\n6. Worker modifying bid...");
        const updateBidRes = await req('PUT', `/bids/${bidId}`, {
            amount: 4000, estimatedDays: 7, message: "Changed my mind, I can do it cheaper!"
        }, workerToken);
        console.log("✅ Bid modified successfully. New amount:", updateBidRes.amount);

        // 7. OWNER FETCHES PROJECT BIDS
        console.log("\n7. Owner fetching project bids...");
        const projectBids = await req('GET', `/bids/project/${projectId}`, null, ownerToken);
        console.log(`✅ Owner fetched bids for project. Found ${projectBids.length} bids.`);

        // 8. OWNER ACCEPTS BID
        console.log("\n8. Owner accepting bid...");
        const acceptRes = await req('PUT', `/bids/${bidId}/accept`, null, ownerToken);
        console.log("✅ Bid accepted successfully. Status:", acceptRes.status);
        
        console.log("\n🎉 ALL TESTS PASSED SUCCESSFULLY!");
    } catch (e) {
        console.error("\n❌ TEST FAILED:", e.message);
    }
}

runTests();
