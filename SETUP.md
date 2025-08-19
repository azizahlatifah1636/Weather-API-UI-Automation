# Project Setup and Configuration Guide

## Quick Start

1. **Clone the repository:**
   ```bash
   git clone https://github.com/azizahlatifah1636/Weather-API-UI-Automation.git
   cd Weather-API-UI-Automation
   ```

2. **Configure API key:**
   ```bash
   cp config.properties.template config.properties
   # Edit config.properties and add your OpenWeatherMap API key
   ```

3. **Run tests:**
   ```bash
   mvn clean test
   mvn allure:serve  # Generate and serve Allure report
   ```

## Environment Setup

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Chrome/Firefox browser
- Internet connection for API calls

### Configuration Files

#### config.properties
```properties
# API Configuration
api.base.url=https://api.openweathermap.org/data/2.5
api.key=YOUR_API_KEY_HERE
api.timeout=30000

# UI Configuration
ui.base.url=https://openweathermap.org
ui.implicit.wait=10
ui.page.load.timeout=30

# Test Configuration
test.city=Jakarta
test.parallel.threads=2
test.retry.count=1
test.temperature.tolerance=3
```

#### Environment Variables
```bash
# Required
export OPENWEATHER_API_KEY="your_api_key_here"

# Optional
export TEST_ENVIRONMENT="production"  # or "staging"
export BROWSER="chrome"              # or "firefox"
export HEADLESS="false"             # or "true"
```

## Repository Secrets Configuration

For CI/CD to work properly, configure these secrets in GitHub repository settings:

### Required Secrets
1. **OPENWEATHER_API_KEY**
   - Value: Your OpenWeatherMap API key
   - Used by: All test workflows

2. **SLACK_WEBHOOK_URL** (Optional)
   - Value: Slack webhook URL for notifications
   - Used by: CI/CD notifications

### Setting up Secrets
1. Go to repository Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Add each secret with the correct name and value

## GitHub Pages Setup

To enable automated report publishing:

1. Go to repository Settings → Pages
2. Source: "GitHub Actions"
3. The workflow will automatically publish reports to Pages

## Branch Protection Rules

Recommended branch protection for main branch:

1. Go to repository Settings → Branches
2. Add rule for "main" branch:
   - Require a pull request before merging
   - Require status checks to pass before merging
   - Required status checks:
     - `test-matrix (11, chrome)`
     - `test-matrix (17, firefox)`
     - `code-quality`
   - Require branches to be up to date before merging
   - Restrict pushes that create files larger than 100MB

## Local Development Workflow

### 1. Create Feature Branch
```bash
git checkout -b feature/weather-validation-improvement
```

### 2. Make Changes
```bash
# Edit code, add tests
mvn clean test  # Run tests locally
mvn spotless:apply  # Format code
```

### 3. Commit and Push
```bash
git add .
git commit -m "feat: improve weather data validation logic"
git push origin feature/weather-validation-improvement
```

### 4. Create Pull Request
- GitHub will automatically run PR checks
- Ensure all checks pass before requesting review

## Test Execution Options

### Run All Tests
```bash
mvn clean test
```

### Run Specific Test Groups
```bash
# API tests only
mvn clean test -Dgroups="api-tests"

# Integration tests only
mvn clean test -Dgroups="integration-tests"

# Smoke tests only
mvn clean test -Dgroups="smoke-tests"
```

### Run with Different Browsers
```bash
# Chrome (default)
mvn clean test -Dbrowser=chrome

# Firefox
mvn clean test -Dbrowser=firefox

# Headless mode
mvn clean test -Dheadless=true
```

### Parallel Execution
```bash
# Run with 3 parallel threads
mvn clean test -DthreadCount=3

# Run specific suite
mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml
```

## Troubleshooting

### Common Issues

#### 1. API Key Not Found
```
Error: OpenWeatherMap API key not configured
Solution: Set OPENWEATHER_API_KEY environment variable or update config.properties
```

#### 2. Browser Driver Issues
```
Error: ChromeDriver not found
Solution: WebDriverManager will auto-download. Ensure internet connection.
```

#### 3. Element Not Found
```
Error: Element not located
Solution: Check if website UI changed. Update locators in WeatherPage.java
```

#### 4. Test Failures Due to Data Variance
```
Error: Temperature difference exceeds tolerance
Solution: Check if tolerance configuration is appropriate (default: ±3°C)
```

### Debug Mode
```bash
# Run with debug logging
mvn clean test -Dlog.level=DEBUG

# Run single test for debugging
mvn clean test -Dtest=WeatherComparisonTest#testWeatherDataConsistency
```

### Code Quality Checks
```bash
# Run SpotBugs analysis
mvn spotbugs:check

# Generate coverage report
mvn jacoco:report

# Format code
mvn spotless:apply
```

## CI/CD Pipeline Details

### Workflow Triggers
- Push to main branch
- Pull requests to main branch
- Manual workflow dispatch
- Scheduled runs (daily at 2 AM UTC)

### Pipeline Stages
1. **Setup**: Java installation, dependency caching
2. **Code Quality**: SpotBugs, formatting check
3. **Test Execution**: Matrix builds (Java 11/17 × Chrome/Firefox)
4. **Security**: Dependency vulnerability scan
5. **Reporting**: Allure report generation and GitHub Pages deployment
6. **Notifications**: Slack alerts on failure

### Monitoring and Alerts
- Failed builds trigger Slack notifications
- GitHub Pages automatically updates with latest reports
- Build status badges in README show current state

## Performance Optimization

### Test Execution Speed
- Parallel execution enabled (2-3 threads)
- Browser reuse between tests
- Optimized locator strategies
- Smart waits instead of fixed delays

### Resource Management
- Dependency caching in CI/CD
- Artifact cleanup after 30 days
- Optimized Docker images for faster startup

## Security Considerations

### API Key Management
- Never commit API keys to repository
- Use environment variables or repository secrets
- Rotate keys regularly

### Dependency Security
- Automated vulnerability scanning with Snyk
- Regular dependency updates
- Security advisories monitoring

## Contact and Support

For questions or issues:
1. Create GitHub issue with bug/feature template
2. Check existing documentation and troubleshooting guide
3. Review CI/CD pipeline logs for build failures

---
*Last updated: Generated automatically*
