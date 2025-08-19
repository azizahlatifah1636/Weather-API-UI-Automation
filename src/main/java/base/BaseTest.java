package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeMethod
    public void setUp() {
        // Pastikan driver terbaru dan bersih
        WebDriverManager.chromedriver().clearDriverCache().setup();
        
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.out.println("Warning: Driver quit timeout, forcing process termination");
                try {
                    // Force kill Chrome processes if driver.quit() times out
                    Runtime.getRuntime().exec("taskkill /F /IM chrome.exe /T");
                    Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe /T");
                } catch (Exception ignored) {}
            }
        }
    }

    public void explicitWait(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
