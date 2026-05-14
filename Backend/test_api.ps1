$body = @{
    name = "Test User"
    email = "test@test.com"
    phone = "1234567890"
    password = "password"
    role = "WORKER"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body $body -ContentType "application/json"
    $response | ConvertTo-Json -Depth 3
} catch {
    $_.Exception.Response
}
