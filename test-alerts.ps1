# Script to test creating multiple alerts with different incident IDs

$baseUrl = "http://localhost:8080"

Write-Host "Creating 5 critical alerts with different incident IDs..." -ForegroundColor Cyan

# Alert 1
Write-Host "Creating alert 1 (INC-001)..." -ForegroundColor Yellow
$response1 = Invoke-WebRequest -Uri "$baseUrl/api/alerts" -Method POST `
  -ContentType "application/json" `
  -Body '{"packetId":"pkt-001","incidentId":"INC-001","severity":"critica","score":0.95,"modelVersion":"v1.2.3"}'
Write-Host $response1.Content
Write-Host ""

# Alert 2
Write-Host "Creating alert 2 (INC-002)..." -ForegroundColor Yellow
$response2 = Invoke-WebRequest -Uri "$baseUrl/api/alerts" -Method POST `
  -ContentType "application/json" `
  -Body '{"packetId":"pkt-002","incidentId":"INC-002","severity":"critica","score":0.92,"modelVersion":"v1.2.3"}'
Write-Host $response2.Content
Write-Host ""

# Alert 3
Write-Host "Creating alert 3 (INC-003)..." -ForegroundColor Yellow
$response3 = Invoke-WebRequest -Uri "$baseUrl/api/alerts" -Method POST `
  -ContentType "application/json" `
  -Body '{"packetId":"pkt-003","incidentId":"INC-003","severity":"alta","score":0.85,"modelVersion":"v1.2.3"}'
Write-Host $response3.Content
Write-Host ""

# Alert 4
Write-Host "Creating alert 4 (INC-004)..." -ForegroundColor Yellow
$response4 = Invoke-WebRequest -Uri "$baseUrl/api/alerts" -Method POST `
  -ContentType "application/json" `
  -Body '{"packetId":"pkt-004","incidentId":"INC-004","severity":"critica","score":0.98,"modelVersion":"v1.2.3"}'
Write-Host $response4.Content
Write-Host ""

# Alert 5
Write-Host "Creating alert 5 (INC-005)..." -ForegroundColor Yellow
$response5 = Invoke-WebRequest -Uri "$baseUrl/api/alerts" -Method POST `
  -ContentType "application/json" `
  -Body '{"packetId":"pkt-005","incidentId":"INC-005","severity":"alta","score":0.78,"modelVersion":"v1.2.3"}'
Write-Host $response5.Content
Write-Host ""

Write-Host "Getting all alerts..." -ForegroundColor Cyan
$allAlerts = Invoke-WebRequest -Uri "$baseUrl/api/alerts?limit=100" -Method GET
Write-Host $allAlerts.Content -ForegroundColor Green
Write-Host "Done!" -ForegroundColor Green
