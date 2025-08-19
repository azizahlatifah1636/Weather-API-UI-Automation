# Weather-API-UI-Automation

# Project Purpose
This repository contains automated tests that compare live weather data from the OpenWeatherMap API with the weather information presented on the OpenWeatherMap website UI. The goal is to verify that the UI reflects API data within realistic tolerances, and to monitor differences that may occur due to timing, caching, or different data sources.

# Summary Metrics (from recent runs)
- Total Tests: 150+
- Pass Rate: 85%
- Failed: 10%
- Skipped: 5%

#  Key Test Insights
- Weather data consistency test completed with revised tolerance (±3°C)
- Temperature extraction strategies use multiple fallback methods
- UI overlay handling implemented for dynamic elements (cookie banners, chat widgets)
- Condition validation changed to flexible matching (no hard failures)
- ChromeDriver compatibility issues were identified and resolved in recent runs

#  Test Execution Summary (examples)
- WeatherComparisonTest.testWeatherDataConsistency — ~45s
- UiTests.testInvalidCityErrorMessage — Skipped (Chrome version mismatch)
- API Temperature Extraction — ~2s
- UI Temperature Parsing — ~15s
- Overlay Removal & Element Search — ~8s

# Test Scenario Details
## Primary Test: Weather Data Consistency
**Objective:** Verify that OpenWeatherMap API and UI provide consistent weather data for Jakarta.

### Test Steps
1. API Data Collection: Retrieve weather data from OpenWeatherMap API.
2. UI Navigation: Open https://openweathermap.org and search for Jakarta.
3. Overlay Handling: Remove or hide blocking elements such as cookie banners and chat widgets.
4. Temperature Extraction: Extract temperature from the UI using multiple fallback strategies (page elements, child elements, page-source regex).
5. Condition Extraction: Extract weather condition text from the UI with filtering (avoid temperature-like strings, promotional text).
6. Data Validation: Compare API vs UI data using realistic tolerances and flexible condition matching.

### Validation Criteria
- Temperature: ±3°C tolerance (PRIMARY validation)
- Condition: Flexible matching with related conditions (SECONDARY validation)
- Timing: Monitor time gaps between API and UI reads for variance analysis

### Recent Improvements
- Separated temperature and condition element searches
- Added robust parsing with multiple locator strategies
- Implemented environmental awareness (timing gaps, data source differences)
- Enhanced error handling with graceful fallbacks
- Added detailed logging for troubleshooting

#  Technical Architecture
## Framework Components
- Java 11 + Maven: project and dependency management
- TestNG: test execution framework
- Selenium WebDriver 4.11.0: UI automation (Chrome)
- RestAssured 5.3.0: API testing
- Allure 2.20.1: test reporting and documentation
- WebDriverManager 5.5.3: automatic driver management

## Page Object Pattern
- `WeatherPage`: UI interactions (search, temperature extraction, condition extraction)
- `WeatherApiClient`: API calls and response handling
- `BaseTest`: WebDriver lifecycle management and common setup/teardown

## Robust Element Location Strategies
- CSS selectors for common patterns
- XPath with text content matching
- Page source regex extraction as fallback
- JavaScript execution to remove/hide overlays

#  Test Evolution & Lessons Learned
## Challenge Resolution Timeline
- Initial Issue: Degree symbol encoding failures in Windows Cp1252
- UI Complexity: Dynamic overlays and suggestion dropdowns
- Element Instability: Promotional text and UI labels interfering with weather data
- Data Variance: Weather data legitimately differs between sources
- Test Philosophy: Evolving from strict equality to tolerance-based monitoring

## Key Insights
- Real-time weather testing requires tolerance for natural variance
- API and UI sources can legitimately differ due to timing and caching
- Robust element location needs multiple fallback strategies
- Environmental awareness (timestamps, location precision) is crucial for comparisons
- Maintain tests by adapting to UI changes and data characteristics

# How to Run
## Prerequisites
- Java 11+
- Maven
- Google Chrome (compatible version) — WebDriverManager used to handle drivers

## Run Tests
- Run all tests: `mvn test`
- Run a specific test: `mvn -Dtest=tests.WeatherComparisonTest#testWeatherDataConsistency test`

## Generate Allure Report
- With Maven plugin: `mvn allure:report` (output: `target/site/allure-maven-plugin`)
- With Allure CLI (if installed): `allure generate allure-results -o allure-report` then `allure open allure-report`
- A simple HTML summary is available at `allure-report.html` in the project root

# Security Notes
- Move the API key to `config.properties` or environment variables — do not commit secrets.
- Do not share personal access tokens publicly. If you accidentally posted a token, revoke it immediately.

# Contributing
- Fork, create a branch, make changes and open a PR. Keep tests stable and add logging for flaky behavior.

# License
MIT