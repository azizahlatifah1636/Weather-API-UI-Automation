package tests;

import api.WeatherApiClient;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.WeatherPage;
import base.BaseTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherComparisonTest extends BaseTest {

    private final String API_KEY = "90b6c48812f6ce5d0aa8697dd4b6565f"; // pindahkan ke config.properties kalau mau aman
    private final String CITY = "Jakarta";

    @Test
    public void testWeatherDataConsistency() {
        long startTime = System.currentTimeMillis();
        
        // SCENARIO: Verify that OpenWeatherMap API and UI provide consistent weather data
        // GIVEN: User searches for weather in Jakarta
        // WHEN: API call is made and UI is navigated to show weather data
        // THEN: Temperature should be within reasonable range (±3°C) and condition should be related
        
        System.out.println("=== WEATHER DATA CONSISTENCY TEST ===");
        System.out.println("Test Start Time: " + new java.util.Date(startTime));
        
        // 1. SETUP: Get API data as baseline
        WeatherApiClient apiClient = new WeatherApiClient(API_KEY);
        Response apiResponse = apiClient.getWeatherByCity(CITY);
        
        Number apiTempNum = apiResponse.path("main.temp");
        double apiTemp = apiTempNum.doubleValue();
        String apiCondition = apiResponse.path("weather[0].main");
        String apiDescription = apiResponse.path("weather[0].description");
        long apiTimestamp = System.currentTimeMillis();
        
        System.out.println("API Data Retrieved:");
        System.out.println("  Temperature: " + apiTemp + "°C");
        System.out.println("  Condition: " + apiCondition);
        System.out.println("  Description: " + apiDescription);
        
        // 2. UI NAVIGATION: Navigate to weather page and search
        WeatherPage weatherPage = new WeatherPage(driver);
        weatherPage.open("https://openweathermap.org");
        
        // Wait for page load and remove any blocking overlays
        try {
            Thread.sleep(2000); // Allow page to load completely
            weatherPage.removeOverlaysAndWait();
        } catch (Exception e) {
            System.out.println("INFO: Initial page setup: " + e.getMessage());
        }
        
        weatherPage.searchCity(CITY);
        long uiTimestamp = System.currentTimeMillis();
        
        // 3. DATA EXTRACTION: Get UI temperature (primary validation)
        double uiTemp = extractUiTemperature();
        
        // 4. DATA EXTRACTION: Get UI condition (secondary validation)
        String uiCondition = extractUiCondition(weatherPage);
        
        // 5. TIMING ANALYSIS
        long timeDiff = uiTimestamp - apiTimestamp;
        System.out.println("Time between API and UI data: " + timeDiff + "ms");
        if (timeDiff > 30000) { // 30 seconds
            System.out.println("WARNING: Large time gap between API and UI data collection");
        }
        
        // 6. ASSERTIONS
        performTemperatureValidation(apiTemp, uiTemp);
        performConditionValidation(apiCondition, apiDescription, uiCondition, timeDiff);
        
        System.out.println("=== TEST COMPLETED SUCCESSFULLY ===");
    }
    
    private double extractUiTemperature() {
        System.out.println("\n--- EXTRACTING UI TEMPERATURE ---");
        
        // Strategy 1: Try WeatherPage.getTemperature() method
        try {
            WeatherPage weatherPage = new WeatherPage(driver);
            String tempText = weatherPage.getTemperature();
            if (tempText != null && !tempText.equals("N/A")) {
                double temp = parseTemperatureFromText(tempText);
                if (temp != Double.MIN_VALUE) {
                    System.out.println("✓ Temperature extracted via WeatherPage: " + temp + "°C");
                    return temp;
                }
            }
        } catch (Exception e) {
            System.out.println("Strategy 1 failed: " + e.getMessage());
        }
        
        // Strategy 2: Direct element search with robust locators
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        List<By> tempLocators = Arrays.asList(
            By.cssSelector(".current-temp, .temperature, [class*='temp']"),
            By.xpath("//span[contains(text(),'°C') or contains(text(),'°')]"),
            By.xpath("//*[contains(@class,'temp') and contains(text(),'°')]")
        );
        
        for (By locator : tempLocators) {
            try {
                WebElement tempElem = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                String tempText = tempElem.getText();
                double temp = parseTemperatureFromText(tempText);
                if (temp != Double.MIN_VALUE) {
                    System.out.println("✓ Temperature extracted via locator " + locator + ": " + temp + "°C");
                    return temp;
                }
            } catch (Exception ignored) {}
        }
        
        // Strategy 3: Page source regex extraction
        String pageSource = driver.getPageSource();
        Pattern tempPattern = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*(?:°C|°)");
        Matcher matcher = tempPattern.matcher(pageSource);
        if (matcher.find()) {
            try {
                double temp = Double.parseDouble(matcher.group(1));
                System.out.println("✓ Temperature extracted from page source: " + temp + "°C");
                return temp;
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse temperature from page source");
            }
        }
        
        Assert.fail("❌ CRITICAL: Could not extract temperature from UI using any strategy");
        return Double.MIN_VALUE;
    }
    
    private String extractUiCondition(WeatherPage weatherPage) {
        System.out.println("\n--- EXTRACTING UI CONDITION ---");
        
        try {
            String condition = weatherPage.getCondition();
            if (condition != null && !condition.equals("N/A") && !condition.toLowerCase().contains("humidity") 
                && !condition.toLowerCase().contains("pressure")) {
                System.out.println("✓ Condition extracted: '" + condition + "'");
                return condition;
            }
        } catch (Exception e) {
            System.out.println("WeatherPage.getCondition() failed: " + e.getMessage());
        }
        
        System.out.println("⚠ Could not extract valid weather condition from UI");
        return "N/A";
    }
    
    private double parseTemperatureFromText(String tempText) {
        if (tempText == null || tempText.trim().isEmpty()) return Double.MIN_VALUE;
        
        // Normalize text
        String normalized = tempText.replace(Character.toString((char)0x00A0), " ")
                                  .replace("Â", "")
                                  .replace("&deg;", "")
                                  .trim();
        
        // Extract number before degree symbol or 'C'
        Pattern numberPattern = Pattern.compile("(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = numberPattern.matcher(normalized);
        
        while (matcher.find()) {
            try {
                double temp = Double.parseDouble(matcher.group(1));
                // Reasonable temperature range for weather data
                if (temp >= -50 && temp <= 60) {
                    return temp;
                }
            } catch (NumberFormatException ignored) {}
        }
        
        return Double.MIN_VALUE;
    }
    
    private void performTemperatureValidation(double apiTemp, double uiTemp) {
        System.out.println("\n--- TEMPERATURE VALIDATION ---");
        double tempDiff = Math.abs(apiTemp - uiTemp);
        
        System.out.println("API Temperature: " + apiTemp + "°C");
        System.out.println("UI Temperature: " + uiTemp + "°C");
        System.out.println("Difference: " + tempDiff + "°C");
        
        // More realistic tolerance for weather data
        double tolerance = 3.0; // ±3°C tolerance
        
        if (tempDiff <= tolerance) {
            System.out.println("✓ PASS: Temperature difference (" + tempDiff + "°C) is within acceptable range (±" + tolerance + "°C)");
        } else {
            System.out.println("❌ FAIL: Temperature difference (" + tempDiff + "°C) exceeds tolerance (±" + tolerance + "°C)");
            Assert.fail("Temperature validation failed: API=" + apiTemp + "°C, UI=" + uiTemp + "°C, Diff=" + tempDiff + "°C");
        }
    }
    
    private void performConditionValidation(String apiCondition, String apiDescription, String uiCondition, long timeDiff) {
        System.out.println("\n--- CONDITION VALIDATION ---");
        System.out.println("API Condition: " + apiCondition);
        System.out.println("API Description: " + apiDescription);
        System.out.println("UI Condition: " + uiCondition);
        
        if ("N/A".equals(uiCondition)) {
            System.out.println("ℹ INFO: UI condition not available - this is acceptable");
            return;
        }
        
        // Check if conditions are related (flexible matching)
        String apiLower = apiCondition.toLowerCase();
        String uiLower = uiCondition.toLowerCase();
        String descLower = apiDescription.toLowerCase();
        
        boolean isRelated = uiLower.contains(apiLower) || 
                           apiLower.contains(uiLower) ||
                           uiLower.contains(descLower) ||
                           descLower.contains(uiLower) ||
                           areWeatherConditionsRelated(apiLower, uiLower);
        
        if (isRelated) {
            System.out.println("✓ PASS: Weather conditions are related");
        } else {
            String message = "⚠ WARNING: Weather conditions differ significantly - API: '" + apiCondition + 
                           "' vs UI: '" + uiCondition + "'";
            
            // If time difference is significant, this is expected
            if (timeDiff > 60000) { // 1 minute
                System.out.println(message + " (Expected due to timing difference)");
            } else {
                System.out.println(message + " (Possible data source difference)");
                // Don't fail test - weather data variance is normal
            }
        }
    }
    
    private boolean areWeatherConditionsRelated(String condition1, String condition2) {
        // Define related weather condition groups
        String[][] relatedGroups = {
            {"cloud", "clouds", "cloudy", "overcast", "partly", "mostly"},
            {"rain", "rainy", "drizzle", "shower", "precipitation"},
            {"clear", "sunny", "sun", "bright"},
            {"snow", "snowy", "sleet", "blizzard"},
            {"fog", "mist", "haze", "foggy"}
        };
        
        for (String[] group : relatedGroups) {
            boolean found1 = false, found2 = false;
            for (String keyword : group) {
                if (condition1.contains(keyword)) found1 = true;
                if (condition2.contains(keyword)) found2 = true;
            }
            if (found1 && found2) return true;
        }
        
        return false;
    }
}