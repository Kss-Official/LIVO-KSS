$ErrorActionPreference = 'Stop'
$baseUrl = 'http://localhost:8081'

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "   LIVO BACKEND FULL END-TO-END HTTP API TEST RUNNER " -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan

# 1. Auth Sync
Write-Host "[1/10] Testing POST /api/v1/auth/sync..." -NoNewline
$authBody = @{
    firebaseUid = "test-e2e-uid-" + [System.Guid]::NewGuid().ToString()
    email = "e2e-test-" + [System.Guid]::NewGuid().ToString().Substring(0,8) + "@livo.app"
    fullName = "LIVO Automated Tester"
    avatarUrl = "https://livo.app/avatar.png"
} | ConvertTo-Json

$authResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/sync" -Method Post -Body $authBody -ContentType "application/json"
$token = $authResp.data.accessToken
$headers = @{
    Authorization = "Bearer $token"
    "Content-Type" = "application/json"
}
Write-Host " PASS (JWT Acquired, Status 201/200)" -ForegroundColor Green

# 2. Auth Me
Write-Host "[2/10] Testing GET /api/v1/auth/me..." -NoNewline
$meResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/me" -Method Get -Headers $headers
Write-Host " PASS (User: $($meResp.data.fullName) | Life Areas: $($meResp.data.lifeAreas.Count))" -ForegroundColor Green

# 3. Universal Add Quick Capture
Write-Host "[3/10] Testing POST /api/v1/universal-add/parse..." -NoNewline
$parseBody = @{
    input = "Buy groceries tomorrow at 5pm #errands !high"
    useAi = $false
} | ConvertTo-Json
$parseResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/universal-add/parse" -Method Post -Headers $headers -Body $parseBody
Write-Host " PASS (Detected Domain: $($parseResp.data.detectedDomain) | Title: $($parseResp.data.title))" -ForegroundColor Green

# 4. Tasks Module (Create & List)
Write-Host "[4/10] Testing POST & GET /api/v1/tasks..." -NoNewline
$taskBody = @{
    title = "Automated E2E Verification Task"
    description = "Testing tasks lifecycle end-to-end"
    priority = "HIGH"
    dueDate = (Get-Date).AddDays(1).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    estimatedMinutes = 45
} | ConvertTo-Json
$taskResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/tasks" -Method Post -Headers $headers -Body $taskBody
$taskId = $taskResp.data.id

$tasksList = Invoke-RestMethod -Uri "$baseUrl/api/v1/tasks" -Method Get -Headers $headers
Write-Host " PASS (Task ID: $taskId | Total Tasks: $($tasksList.data.content.Count))" -ForegroundColor Green

# 5. Habits Module (Create & List)
Write-Host "[5/10] Testing POST & GET /api/v1/habits..." -NoNewline
$habitBody = @{
    title = "Automated Habit Consistency"
    description = "Daily consistency check"
    frequency = "DAILY"
    targetDaysPerWeek = 7
    color = "#10B981"
} | ConvertTo-Json
$habitResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/habits" -Method Post -Headers $headers -Body $habitBody
$habitId = $habitResp.data.id

$habitsList = Invoke-RestMethod -Uri "$baseUrl/api/v1/habits" -Method Get -Headers $headers
Write-Host " PASS (Habit ID: $habitId | Total Habits: $($habitsList.data.Count))" -ForegroundColor Green

# 6. Schedule Module (Fixed Blocks & Daily Plan)
Write-Host "[6/10] Testing Schedule Blocks & Daily Plan..." -NoNewline
$blockBody = @{
    title = "Sprint Planning Sync"
    blockDate = (Get-Date).ToString("yyyy-MM-dd")
    startTime = "09:00:00"
    endTime = "10:00:00"
    category = "WORK"
    isLocked = $true
} | ConvertTo-Json
$blockResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/schedule-blocks" -Method Post -Headers $headers -Body $blockBody

$planResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/plan/daily?date=$((Get-Date).ToString('yyyy-MM-dd'))" -Method Get -Headers $headers
Write-Host " PASS (Block ID: $($blockResp.data.id) | Blocks in Plan: $($planResp.data.blocks.Count))" -ForegroundColor Green

# 7. Goals Module (Create & List)
Write-Host "[7/10] Testing POST & GET /api/v1/goals..." -NoNewline
$goalBody = @{
    title = "Achieve 100% Backend API Test Coverage"
    category = "CAREER"
    targetDate = (Get-Date).AddMonths(1).ToString("yyyy-MM-dd")
    targetMetric = "Percent"
    targetValue = 100.0
} | ConvertTo-Json
$goalResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/goals" -Method Post -Headers $headers -Body $goalBody
$goalsList = Invoke-RestMethod -Uri "$baseUrl/api/v1/goals" -Method Get -Headers $headers
Write-Host " PASS (Goal ID: $($goalResp.data.id) | Total Goals: $($goalsList.data.Count))" -ForegroundColor Green

# 8. Finance Module (Transactions & Summary)
Write-Host "[8/10] Testing Finance Transactions & Summary..." -NoNewline
$txBody = @{
    title = "Cloud hosting server"
    description = "Monthly hosting infrastructure"
    amount = 120.50
    currency = "USD"
    type = "EXPENSE"
    category = "Development"
    paymentMethod = "CREDIT_CARD"
    transactionDate = (Get-Date).ToString("yyyy-MM-dd")
    transactionTime = "12:00:00"
} | ConvertTo-Json
$txResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/finance/transactions" -Method Post -Headers $headers -Body $txBody

$finSummary = Invoke-RestMethod -Uri "$baseUrl/api/v1/finance/summary" -Method Get -Headers $headers
Write-Host " PASS (Tx ID: $($txResp.data.id) | Total Expenses: $($finSummary.data.totalExpenses))" -ForegroundColor Green

# 9. Home / Today Dashboard Aggregator
Write-Host "[9/10] Testing GET /api/v1/home (Unified Dashboard)..." -NoNewline
$homeResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/home" -Method Get -Headers $headers
Write-Host " PASS (Today Timeline: $($homeResp.data.timeline.Count) | Habits: $($homeResp.data.habits.Count))" -ForegroundColor Green

# 10. Delta Offline Sync Engine (Pull)
Write-Host "[10/10] Testing GET /api/v1/sync/pull (Delta Engine)..." -NoNewline
$syncResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/sync/pull" -Method Get -Headers $headers
Write-Host " PASS (Domains Synced: Tasks=$($syncResp.data.tasks.Count), Habits=$($syncResp.data.habits.Count))" -ForegroundColor Green

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host " ALL 10 E2E DOMAIN SUITES PASSED ON LIVE SERVER!     " -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Cyan
