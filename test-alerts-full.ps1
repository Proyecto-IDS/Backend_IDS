# Script to test creating multiple alerts

$baseUrl = "http://localhost:8080"

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Testing Alerts with New Backend" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Create 10 alerts with different IDs
for ($i = 1; $i -le 10; $i++) {
    $incidentId = "INC-TEST-$i"
    $packetId = "PKT-TEST-$i"
    $severity = if ($i % 2 -eq 0) { "critica" } else { "alta" }
    $score = 0.85 + ($i * 0.01)
    
    Write-Host "Creating alert $i ($severity) - Incident: $incidentId" -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/api/alerts" -Method POST `
            -ContentType "application/json" `
            -Body @{
                packetId = $packetId
                incidentId = $incidentId
                severity = $severity
                score = $score
                modelVersion = "v1.2.3"
            } | ConvertTo-Json | ConvertFrom-Json
        
        Write-Host "  ✅ Created with ID: $($response.id)" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ Error: $_" -ForegroundColor Red
    }
    
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "Getting all alerts..." -ForegroundColor Cyan
$allAlerts = Invoke-WebRequest -Uri "$baseUrl/api/alerts?limit=1000" -Method GET | ConvertFrom-Json
Write-Host "Total alerts: $($allAlerts.Count)" -ForegroundColor Green
Write-Host ""

Write-Host "Getting metrics..." -ForegroundColor Cyan
$metrics = Invoke-WebRequest -Uri "$baseUrl/api/alerts/count/by-severity" -Method GET | ConvertFrom-Json
Write-Host "Metrics:" -ForegroundColor Green
Write-Host "  Total: $($metrics.total)" 
Write-Host "  Crítica: $($metrics.critica)"
Write-Host "  Alta: $($metrics.alta)"
