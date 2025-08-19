# PowerShell script to generate Allure report

Write-Host "=== ALLURE REPORT GENERATOR ===" -ForegroundColor Green
Write-Host "Generating Allure report from test results..." -ForegroundColor Yellow

# Check if allure-results directory exists
if (Test-Path "allure-results") {
    Write-Host "✓ Found allure-results directory" -ForegroundColor Green
    
    # Count result files
    $resultFiles = Get-ChildItem -Path "allure-results" -Filter "*.json" | Measure-Object
    Write-Host "✓ Found $($resultFiles.Count) result files" -ForegroundColor Green
    
    # Create allure-report directory if it doesn't exist
    if (!(Test-Path "allure-report")) {
        New-Item -ItemType Directory -Path "allure-report" | Out-Null
        Write-Host "✓ Created allure-report directory" -ForegroundColor Green
    }
    
    # Generate report using Maven
    Write-Host "Generating report using Maven..." -ForegroundColor Yellow
    mvn allure:report
    
    # Check if report was generated
    if (Test-Path "target\site\allure-maven-plugin") {
        Write-Host "✓ Allure report generated successfully!" -ForegroundColor Green
        Write-Host "Report location: target\site\allure-maven-plugin\" -ForegroundColor Cyan
        
        # Open report in default browser
        $reportPath = Resolve-Path "target\site\allure-maven-plugin\index.html" -ErrorAction SilentlyContinue
        if ($reportPath) {
            Write-Host "Opening report in browser..." -ForegroundColor Yellow
            Start-Process $reportPath
        }
    } else {
        Write-Host "❌ Failed to generate report" -ForegroundColor Red
    }
} else {
    Write-Host "❌ allure-results directory not found" -ForegroundColor Red
    Write-Host "Please run tests first: mvn test" -ForegroundColor Yellow
}

Write-Host "=== REPORT GENERATION COMPLETE ===" -ForegroundColor Green
