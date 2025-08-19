package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

import base.BaseTest;

import java.time.Duration;
import java.util.List;

public class WeatherPage extends BaseTest {
    // Page Object Model for Weather UI

    private WebDriver driver;

    // Stable locator for search input (adjust if site changes)
    private By searchInput = By.cssSelector("input[placeholder='Search city']");

    public WeatherPage(WebDriver driver) {
        this.driver = driver;
    }

    public void waitForWeatherElement(By locator) {
        explicitWait(locator);
    }
    
    public void open(String url) {
        driver.get(url);
    }

    // Helper: remove overlays (assistant, banners, cookies) via JS and wait for invisibility
    public void removeOverlaysAndWait() {
        try {
            String js = "var sels=['.ulla-weather-assistant','.assistant','.chat-widget','.cookie-banner','.banner_stripe','.popup','.modal']; sels.forEach(function(s){var els=document.querySelectorAll(s); els.forEach(function(e){ if(e && e.parentNode){ e.parentNode.removeChild(e); } });});";
            ((JavascriptExecutor) driver).executeScript(js);
        } catch (Exception e) {
            System.out.println("DEBUG: removeOverlays JS failed: " + e.getMessage());
        }
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            String[] waitSelectors = new String[]{".cookie-banner", ".ulla-weather-assistant", ".assistant", ".chat-widget", ".banner_stripe"};
            for (String sel : waitSelectors) {
                try {
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(sel)));
                } catch (Exception ignored) { }
            }
        } catch (Exception ignored) { }
    }

    // Refactored searchCity: wait for input, handle cookie banner, clear, send text + try click first suggestion, fallback to ENTER, wait for result
    public void searchCity(String city) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // 1. Handle cookie banner if present with case-insensitive matching
        try {
            By acceptCookie = By.xpath("//button[contains(translate(text(),'ACCEPT','accept'),'accept') or contains(translate(text(),'AGREE','agree'),'agree')]");
            WebElement acceptBtn = wait.withTimeout(Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(acceptCookie));
            acceptBtn.click();
            Thread.sleep(500); // Brief pause after closing banner
            System.out.println("INFO: Cookie banner closed");
        } catch (Exception ignored) {
            System.out.println("INFO: No cookie banner found or not clickable");
        }

        // 2. Type in search box and wait for suggestions
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        searchBox.clear();
        searchBox.sendKeys(city);
        
        try {
            // Wait briefly for suggestions to load
            Thread.sleep(1500);
            
            // Try multiple suggestion list locators
            By[] suggestionLocators = {
                By.cssSelector(".search-dropdown-menu li"),
                By.cssSelector("ul.search-results-list li"),
                By.cssSelector(".search-suggestions li"),
                By.cssSelector("ul.suggestions-list li"),
                By.cssSelector(".suggest-list li"),
                By.xpath("//ul[contains(@class,'suggest') or contains(@class,'search')]//li[contains(text(),'" + city + "')]")
            };
            
            WebElement suggestionToClick = null;
            for (By locator : suggestionLocators) {
                try {
                    List<WebElement> suggestions = driver.findElements(locator);
                    for (WebElement suggestion : suggestions) {
                        if (suggestion.isDisplayed() && suggestion.getText().toLowerCase().contains(city.toLowerCase())) {
                            suggestionToClick = suggestion;
                            break;
                        }
                    }
                    if (suggestionToClick != null) break;
                } catch (Exception e) {
                    continue;
                }
            }
            
            if (suggestionToClick != null) {
                wait.until(ExpectedConditions.elementToBeClickable(suggestionToClick)).click();
                System.out.println("INFO: Clicked matching suggestion for: " + city);
            } else {
                System.out.println("INFO: No matching suggestion found, pressing ENTER");
                searchBox.sendKeys(Keys.ENTER);
            }

            // Remove overlays after interacting with the page to avoid assistant/banner blocking results
            removeOverlaysAndWait();
            
        } catch (Exception e) {
            System.out.println("INFO: Failed to handle suggestions: " + e.getMessage() + ". Falling back to ENTER.");
            searchBox.sendKeys(Keys.ENTER);
            removeOverlaysAndWait();
        }

        // 3. Wait for results with multiple strategies
        try {
            // First try specific weather widgets
            By[] weatherLocators = {
                By.cssSelector(".current-weather"),
                By.cssSelector(".weather-widget"),
                By.cssSelector("[class*='temperature']"),
                By.cssSelector(".weather-info"),
                By.xpath("//*[contains(@class, 'weather')]//span[contains(text(),'°')]") ,
                By.xpath("//*[contains(text(),'°C') or contains(text(),'\u00B0C')]")
            };

            boolean found = false;
            for (By locator : weatherLocators) {
                try {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                    found = true;
                    System.out.println("INFO: Weather results found with locator: " + locator);
                    break;
                } catch (Exception ignored) {
                    // Try next locator
                }
            }

            if (!found) {
                throw new RuntimeException("Weather results not visible after search");
            }
        } catch (Exception e) {
            System.out.println("WARNING: Standard weather containers not found: " + e.getMessage());
            // Final fallback: wait for ANY element containing temperature or number followed by C
            try {
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'°') or contains(text(),'°C')]")),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'\u00B0') or contains(text(),'\u00B0C')]")),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'C') and string-length(text()) < 10]")),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//span[matches(text(), '[0-9]+.*C')]"))
                ));
                System.out.println("INFO: Found weather element with fallback locators");
            } catch (Exception ex) {
                System.out.println("WARNING: Even fallback weather containers not found. Will try to continue anyway.");
                // Just continue and hope for the best
            }
        }
    }

    public String getTemperature() {
        try {
            System.out.println("Trying to find temperature element...");
            // Try multiple temperature locators with expanded options
            By[] tempLocators = {
                // CSS selectors for common temperature classes
                By.cssSelector(".temperature"),
                By.cssSelector(".current-temp"),
                By.cssSelector("[class*='temp']"),
                By.cssSelector(".temp"),
                By.cssSelector(".current-container .heading"),
                
                // Looking for elements with numeric content + C
                By.xpath("//span[matches(text(), '\\d+.*C')]"),
                By.xpath("//div[matches(text(), '\\d+.*C')]"),
                
                // Looking for elements containing degree symbols in different encodings
                By.xpath("//*[contains(text(),'°C') or contains(text(),'°')]"),
                By.xpath("//*[contains(text(),'\u00B0C') or contains(text(),'\u00B0')]"),
                
                // Temperature with number patterns
                By.xpath("//*[matches(text(), '-?\\d+(\\.\\d+)?\\s*°?C?')]")
            };
            
            for (By locator : tempLocators) {
                try {
                    List<WebElement> elements = driver.findElements(locator);
                    for (WebElement elem : elements) {
                        String text = elem.getText().trim();
                        if (text.length() > 0 && text.matches(".*\\d+.*")) {
                            System.out.println("Found temperature using: " + locator + ", text: " + text);
                            return text;
                        }
                    }
                } catch (Exception ignored) {
                    // Try next locator
                }
            }
            
            // Fallback to page source extraction with more patterns
            System.out.println("Falling back to page source extraction for temperature...");
            String pageSource = driver.getPageSource();
            
            // Try multiple regex patterns for temperature extraction
            String[] patterns = {
                "(-?\\d+(?:\\.\\d+)?)\\s*(?:°C|\\u00B0C|°)",
                "(-?\\d+(?:\\.\\d+)?)\\s*(?:C)",
                "temperature[^>]*>(\\d+(?:\\.\\d+)?)",
                "temp[^>]*>(\\d+(?:\\.\\d+)?)",
                "(\\d+)\\s*(?:°|\\u00B0|&deg;)(?:C)?"
            };
            
            for (String patternStr : patterns) {
                try {
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr);
                    java.util.regex.Matcher matcher = pattern.matcher(pageSource);
                    if (matcher.find()) {
                        String result = matcher.group(0);
                        System.out.println("Found temperature in page source using pattern: " + patternStr + ", result: " + result);
                        return result;
                    }
                } catch (Exception e) {
                    System.out.println("Pattern failed: " + patternStr + " - " + e.getMessage());
                }
            }
            
            // Last resort: look for ANY number followed by C or any number with a span nearby
            java.util.regex.Pattern lastPattern = java.util.regex.Pattern.compile("(\\d+)[^<>]{0,5}C");
            java.util.regex.Matcher lastMatcher = lastPattern.matcher(pageSource);
            if (lastMatcher.find()) {
                String result = lastMatcher.group(0);
                System.out.println("Last resort found temperature: " + result);
                return result;
            }
            
            System.out.println("WARNING: Could not find temperature with any method");
            return "N/A";
        } catch (Exception e) {
            System.out.println("Error getting temperature: " + e.getMessage());
            return "N/A";
        }
    }

    public String getCondition() {
        try {
            String DEG = Character.toString((char)0x00B0);

            // Promotional tokens often found in ad/call-to-action text
            String[] promoTokens = {"professional","collection","collections","dashboard","dashboards","subscribe","pricing","offer","advanced","specialis","signup","register","free trial","get started","extend","premium","specialised","specialized"};
            String[] weatherKeywords = {"cloud","clear","rain","sun","overcast","mist","drizzle","thunder","fog","snow","haze","few clouds","broken","scattered","gentle breeze"};

            // Only search inside these scoped containers to avoid ads/banners
            By[] scopedContainers = new By[] {
                By.cssSelector(".current-container"),
                By.cssSelector(".current-weather"),
                By.cssSelector("#weather-widget"),
                By.cssSelector("main"),
                By.cssSelector(".right")
            };

            // Remove overlays like assistant or cookie banner before locating condition elements
            removeOverlaysAndWait();
            
            for (By containerLocator : scopedContainers) {
                try {
                    List<WebElement> containers = driver.findElements(containerLocator);
                    for (WebElement container : containers) {
                        if (container == null) continue;

                        // Prefer specific elements inside container
                        List<WebElement> candidates = container.findElements(By.cssSelector(".weather-description, .description, p, span"));
                        for (WebElement cand : candidates) {
                            try {
                                if (cand == null) continue;
                                String raw = cand.getText();
                                if (raw == null) continue;
                                String norm = raw.replace(Character.toString((char)0x00A0), " ").replace("Â", "").replace("&deg;",""
                                ).trim();
                                String lower = norm.toLowerCase();

                                // Skip numeric/temperature-like values
                                if (lower.matches(".*\\d.*")) {
                                    System.out.println("DEBUG: Candidate contains digits, skipping. outerHTML=" + cand.getAttribute("outerHTML"));
                                    continue;
                                }
                                if (lower.contains("" + DEG)) {
                                    System.out.println("DEBUG: Candidate contains degree symbol, skipping. outerHTML=" + cand.getAttribute("outerHTML"));
                                    continue;
                                }

                                // Skip obvious promotional text by token
                                boolean promo = false;
                                for (String p : promoTokens) {
                                    if (lower.contains(p)) { promo = true; break; }
                                }
                                if (promo) {
                                    System.out.println("DEBUG: Candidate looks promotional, skipping. outerHTML=" + cand.getAttribute("outerHTML"));
                                    continue;
                                }

                                // If candidate contains a weather keyword, accept it
                                for (String kw : weatherKeywords) {
                                    if (lower.contains(kw)) {
                                        String first = norm.split("\\r?\\n")[0].split("\\.")[0].trim();
                                        if (first.length() >= 3 && first.length() < 120) {
                                            System.out.println("INFO: Selected condition from scoped container '" + containerLocator + "' -> '" + first + "' (outerHTML=" + cand.getAttribute("outerHTML") + ")");
                                            return first;
                                        }
                                    }
                                }

                                // Skip common weather UI labels (these are not conditions)
                                String[] labelPatterns = {"humidity","pressure","visibility","wind","uv","dewpoint","feels like","sunrise","sunset","today","tomorrow","now","current","temperature","temp"};
                                boolean isLabel = false;
                                for (String label : labelPatterns) {
                                    if (lower.equals(label) || lower.equals(label + ":") || lower.startsWith(label + ":")) {
                                        isLabel = true;
                                        break;
                                    }
                                }
                                if (isLabel) {
                                    System.out.println("DEBUG: Candidate is a UI label, skipping. outerHTML=" + cand.getAttribute("outerHTML"));
                                    continue;
                                }

                                // Conservative fallback removed to avoid selecting non-weather text

                                System.out.println("DEBUG: Candidate did not match criteria, skipped. outerHTML=" + cand.getAttribute("outerHTML"));

                            } catch (Exception ignoredCandidate) { }
                        }
                    }
                } catch (Exception ignoredContainer) { }
            }

            // No broad page-wide fallback to avoid picking promotional content
            System.out.println("WARNING: No valid condition found inside scoped containers. Returning N/A.");
            return "N/A";
        } catch (Exception e) {
            System.out.println("ERROR: Error getting condition: " + e.getMessage());
            return "N/A";
        }
    }

    public void explicitWait(By locator) {
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
