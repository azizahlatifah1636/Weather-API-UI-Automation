package tests;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import pages.WeatherPage;
import base.BaseTest;

public class UiTests extends BaseTest {

    @Test(enabled = false)
    public void testInvalidCityErrorMessage() {
        // test ini sementara dinonaktifkan
        WeatherPage weatherPage = new WeatherPage(driver);
        By errorMessageLocator = By.id("error-message");

        // Ensure the page is loaded and log the page source for debugging
        weatherPage.open("https://openweathermap.org");
        explicitWait(By.cssSelector("input[type='text']")); // Wait for the search input to be visible
        explicitWait(By.xpath("//span[contains(text(),'°C')]")); // Wait for the temperature element to be visible

        // Search for an invalid city to trigger the error message
        weatherPage.searchCity("InvalidCity123");

        // Wait for the error message
        weatherPage.waitForWeatherElement(errorMessageLocator);
        // Additional assertions for error message
        // Example: Assert.assertTrue(driver.findElement(errorMessageLocator).isDisplayed(), "Error message not displayed");
    }
}
