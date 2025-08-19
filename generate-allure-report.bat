@echo off
echo =====================================
echo    ALLURE REPORT GENERATOR
echo =====================================
echo.

echo [1/4] Cleaning previous reports...
if exist "target\site\allure-maven-plugin" rmdir /s /q "target\site\allure-maven-plugin"

echo [2/4] Checking allure-results directory...
if not exist "allure-results" (
    echo ERROR: allure-results directory not found!
    echo Please run tests first: mvn test
    pause
    exit /b 1
)

echo [3/4] Generating Allure report using Maven plugin...
call mvn allure:report

echo [4/4] Opening report...
if exist "target\site\allure-maven-plugin\index.html" (
    echo SUCCESS: Report generated successfully!
    echo Opening report in default browser...
    start "" "target\site\allure-maven-plugin\index.html"
) else (
    echo WARNING: Report not found. Opening custom report...
    start "" "allure-report.html"
)

echo.
echo =====================================
echo Report generation complete!
echo =====================================
pause
