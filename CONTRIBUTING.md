# Weather API-UI Automation - Development Guide

## 🚀 Getting Started for Contributors

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Git
- Chrome/Firefox browser
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Local Setup
1. **Clone the repository**
   ```bash
   git clone https://github.com/azizahlatifah1636/Weather-API-UI-Automation.git
   cd Weather-API-UI-Automation
   ```

2. **Create config file**
   ```bash
   cp config.properties.template config.properties
   # Add your OpenWeatherMap API key
   echo "api.key=YOUR_API_KEY_HERE" >> config.properties
   ```

3. **Install dependencies**
   ```bash
   mvn clean install
   ```

4. **Run tests locally**
   ```bash
   mvn test
   ```

## 🌿 Branching Strategy

### Branch Types
- **`main`** - Production-ready code, protected branch
- **`develop`** - Integration branch for features
- **`feature/`** - Feature development (e.g., `feature/api-validation`)
- **`bugfix/`** - Bug fixes (e.g., `bugfix/chrome-driver-issue`)
- **`hotfix/`** - Critical production fixes
- **`release/`** - Release preparation

### Workflow
1. Create feature branch from `develop`
2. Develop and test locally
3. Create Pull Request to `develop`
4. Code review and CI checks
5. Merge to `develop`
6. Release from `develop` to `main`

## 📝 Pull Request Guidelines

### PR Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows project conventions
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No new warnings introduced
```

### Code Review Criteria
- ✅ **Functionality**: Code works as expected
- ✅ **Performance**: No performance degradation
- ✅ **Security**: No security vulnerabilities
- ✅ **Maintainability**: Code is readable and well-structured
- ✅ **Test Coverage**: Adequate test coverage
- ✅ **Documentation**: Code is properly documented

## 🧪 Testing Standards

### Test Categories
1. **Unit Tests** - Fast, isolated component tests
2. **Integration Tests** - API and service integration
3. **E2E Tests** - Full user journey validation
4. **Performance Tests** - Load and response time validation

### Test Naming Convention
```java
// Pattern: should[ExpectedBehavior]When[StateUnderTest]
@Test
public void shouldReturnTemperatureWhenValidCityProvided() {
    // test implementation
}

@Test
public void shouldHandleInvalidApiKeyGracefully() {
    // test implementation
}
```

### Test Data Management
- Use test data builders for complex objects
- Mock external dependencies
- Use property files for test configuration
- Clean up test data after execution

## 🔧 Code Standards

### Java Conventions
- Follow Google Java Style Guide
- Use meaningful variable and method names
- Add JavaDoc for public methods
- Keep methods small and focused
- Use appropriate design patterns

### Example Code Structure
```java
/**
 * Validates weather data consistency between API and UI
 * @param city The city name to validate
 * @return ValidationResult containing comparison details
 */
public ValidationResult validateWeatherConsistency(String city) {
    // Implementation with proper error handling
    try {
        WeatherData apiData = weatherApiClient.getWeatherData(city);
        WeatherData uiData = weatherPage.extractWeatherData(city);
        return weatherValidator.compare(apiData, uiData);
    } catch (Exception e) {
        logger.error("Failed to validate weather data for city: {}", city, e);
        throw new ValidationException("Weather validation failed", e);
    }
}
```

## 🏗️ Architecture Guidelines

### Package Structure
```
src/
├── main/java/
│   ├── api/          # API clients and models
│   ├── pages/        # Page Object Model classes
│   ├── utils/        # Utility classes
│   └── config/       # Configuration management
└── test/java/
    ├── tests/        # Test classes
    ├── fixtures/     # Test data and fixtures
    └── helpers/      # Test helper classes
```

### Design Patterns
- **Page Object Model** for UI interactions
- **Builder Pattern** for test data creation
- **Factory Pattern** for driver management
- **Strategy Pattern** for different validation approaches

## 🚀 CI/CD Pipeline

### Automated Checks
- **Build Verification** - Maven compilation
- **Unit Tests** - Fast feedback loop
- **Integration Tests** - Service interactions
- **Code Quality** - SpotBugs, formatting
- **Security Scan** - Vulnerability detection
- **Test Reports** - Allure report generation

### Deployment Stages
1. **PR Check** - Quick validation on pull requests
2. **Integration** - Full test suite on develop branch
3. **Release** - Production deployment preparation
4. **Monitor** - Post-deployment health checks

## 📊 Monitoring and Reporting

### Allure Reports
- Accessible at: `https://azizahlatifah1636.github.io/Weather-API-UI-Automation/reports`
- Updated automatically after each test run
- Historical data available for trend analysis

### Metrics Tracking
- Test execution time trends
- Pass/fail rate over time
- API response time monitoring
- UI performance metrics

## 🤝 Team Collaboration

### Communication Channels
- **GitHub Issues** - Bug reports and feature requests
- **Pull Requests** - Code review discussions
- **Wiki** - Documentation and guidelines
- **Slack** (if configured) - Real-time notifications

### Code Review Process
1. **Author** - Self-review before submitting PR
2. **Peer Review** - At least one team member review
3. **CI Validation** - All automated checks pass
4. **Approval** - Reviewer approval required
5. **Merge** - Squash and merge to maintain clean history

## 🔒 Security Considerations

### Secrets Management
- Use GitHub Secrets for API keys
- Never commit credentials to repository
- Rotate secrets regularly
- Use environment-specific configurations

### Access Control
- Protected main branch
- Required reviews for PRs
- Branch protection rules
- Limited repository access

## 📈 Performance Optimization

### Test Execution
- Parallel test execution
- Smart test selection
- Resource pooling for drivers
- Efficient data cleanup

### Reporting
- Incremental report generation
- Artifact compression
- Cleanup old reports
- Optimized dashboard loading

## 🛠️ Troubleshooting

### Common Issues
1. **ChromeDriver version mismatch**
   - Use WebDriverManager for automatic management
   - Update driver version in CI configuration

2. **Test flakiness**
   - Add explicit waits
   - Implement retry mechanisms
   - Use stable locators

3. **Environment differences**
   - Use configuration profiles
   - Environment-specific test data
   - Proper test isolation

### Debug Tips
- Enable verbose logging in test environments
- Use screenshot capture on failures
- Implement proper error messages
- Add debugging breakpoints strategically

## 📚 Resources

### Documentation
- [TestNG Documentation](https://testng.org/doc/)
- [Selenium WebDriver Guide](https://selenium-python.readthedocs.io/)
- [Allure Framework](http://allure.qatools.ru/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)

### Tools
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [VS Code with Java Extensions](https://code.visualstudio.com/docs/languages/java)
- [Postman for API Testing](https://www.postman.com/)
- [Chrome DevTools](https://developers.google.com/web/tools/chrome-devtools)

---

**Happy Testing! 🚀**

For questions or support, create an issue in the repository or contact the team leads.
