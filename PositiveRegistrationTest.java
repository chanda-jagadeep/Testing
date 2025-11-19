package com.frugaltests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

/**
 * Positive registration test — robust screenshot saving (overwrites if exists)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PositiveRegistrationTest {
    private WebDriver driver;
    private WebDriverWait wait;

    // <-- set this to the exact URL you're using (http://localhost:8000/main.html or similar) -->
    private final String PAGE = "file:///E:\\Frugal Testing\\index.html";

    @BeforeAll
    public void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Test
    @DisplayName("Positive test: complete form and verify success message")
    public void testSuccessfulRegistration() throws Exception {
        try {
            driver.get(PAGE);

            // fill the form
            driver.findElement(By.id("firstName")).sendKeys("John");
            driver.findElement(By.id("lastName")).sendKeys("Doe");
            driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
            driver.findElement(By.id("phone")).sendKeys("+91 9876543210");
            driver.findElement(By.cssSelector("input[name='gender'][value='Male']")).click();

            // select first real country -> state -> city (index based)
            new Select(driver.findElement(By.id("country"))).selectByIndex(1);
            wait.until(d -> d.findElements(By.cssSelector("#state option")).size() > 1);

            new Select(driver.findElement(By.id("state"))).selectByIndex(1);
            wait.until(d -> d.findElements(By.cssSelector("#city option")).size() > 1);

            new Select(driver.findElement(By.id("city"))).selectByIndex(1);

            driver.findElement(By.id("password")).sendKeys("Abcd@1234");
            driver.findElement(By.id("confirmPassword")).sendKeys("Abcd@1234");

            WebElement terms = driver.findElement(By.id("terms"));
            if (!terms.isSelected()) terms.click();

            // Wait until the submit button is enabled (disabled attribute removed)
            wait.until(d -> {
                WebElement submit = d.findElement(By.id("submitBtn"));
                String disabled = submit.getAttribute("disabled");
                return disabled == null || disabled.isEmpty();
            });

            // Click submit
            driver.findElement(By.id("submitBtn")).click();

            // Wait for success message to appear
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success-msg")));

            // Save screenshot to project folder and overwrite if it already exists
            Path base = Path.of(System.getProperty("user.dir"));
            Path out = base.resolve("success-state.png");
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            try {
                Files.copy(src.toPath(), out, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Positive test passed. Screenshot saved to: " + out.toAbsolutePath());
            } catch (Exception e) {
                // Print a helpful error and fail the test
                System.err.println("Failed to save success screenshot: " + e.getMessage());
                throw new RuntimeException("Could not save success screenshot", e);
            }

            // final assertion to ensure success element is present
            WebElement success = driver.findElement(By.cssSelector(".success-msg"));
            Assertions.assertTrue(success.isDisplayed(), "Expected success message to be visible.");

        } finally {
            if (driver != null) driver.quit();
        }
    }

    @AfterEach
    public void tearDown() {
        // driver already quit in finally, safe to call again
        if (driver != null) driver.quit();
    }
}
