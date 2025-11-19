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
import java.util.List;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NegativeRegistrationTest {
    private WebDriver driver;
    private WebDriverWait wait;

    // <<-- IMPORTANT: set this to the exact URL you use in the browser -->
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));
    }

    @Test
    @DisplayName("Negative test - robust country/state/city handling (no FileAlreadyExists) ")
    public void testMissingLastNameShowsError() throws Exception {
        try {
            driver.get(PAGE);
            System.out.println("Loaded: " + driver.getCurrentUrl() + " | Title: " + driver.getTitle());

            // Wait until country select is populated
            boolean countryPopulated = wait.until(d -> d.findElements(By.cssSelector("#country option")).size() > 1);
            System.out.println("Country populated? " + countryPopulated);
            List<String> countries = driver.findElements(By.cssSelector("#country option")).stream().map(WebElement::getText).collect(Collectors.toList());
            System.out.println("COUNTRY options: " + countries);

            if (countries.size() <= 1) {
                saveDiagnostic("no-country-options.png", "page-source-no-country.html");
                Assertions.fail("Country select not populated. Check script.js loaded correctly.");
            }

            // Select first real country (index 1)
            Select countrySel = new Select(driver.findElement(By.id("country")));
            countrySel.selectByIndex(1);
            System.out.println("Selected country: " + countrySel.getFirstSelectedOption().getText());

            // Dispatch change event to trigger population
            ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('change'))", driver.findElement(By.id("country")));

            // Wait for states then cities
            boolean statesPopulated = wait.until(d -> d.findElements(By.cssSelector("#state option")).size() > 1);
            List<String> states = driver.findElements(By.cssSelector("#state option")).stream().map(WebElement::getText).collect(Collectors.toList());
            System.out.println("STATE options: " + states);

            if (!statesPopulated) {
                saveDiagnostic("no-state-options.png", "page-source-no-state.html");
                Assertions.fail("State select not populated after country change.");
            }

            Select stateSel = new Select(driver.findElement(By.id("state")));
            stateSel.selectByIndex(1);
            System.out.println("Selected state: " + stateSel.getFirstSelectedOption().getText());
            ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('change'))", driver.findElement(By.id("state")));

            boolean citiesPopulated = wait.until(d -> d.findElements(By.cssSelector("#city option")).size() > 1);
            List<String> cities = driver.findElements(By.cssSelector("#city option")).stream().map(WebElement::getText).collect(Collectors.toList());
            System.out.println("CITY options: " + cities);

            if (!citiesPopulated) {
                saveDiagnostic("no-city-options.png", "page-source-no-city.html");
                Assertions.fail("City select not populated after state change.");
            }

            Select citySel = new Select(driver.findElement(By.id("city")));
            citySel.selectByIndex(1);
            System.out.println("Selected city: " + citySel.getFirstSelectedOption().getText());

            // Fill other required fields (leave lastName empty)
            driver.findElement(By.id("firstName")).sendKeys("John");
            driver.findElement(By.id("email")).sendKeys("john.example@test.com");
            driver.findElement(By.id("phone")).sendKeys("+91 9876543210");
            driver.findElement(By.cssSelector("input[name='gender'][value='Male']")).click();
            driver.findElement(By.id("password")).sendKeys("Abcd@1234");
            driver.findElement(By.id("confirmPassword")).sendKeys("Abcd@1234");
            WebElement terms = driver.findElement(By.id("terms"));
            if (!terms.isSelected()) terms.click();

            // Trigger validation for lastName
            ((JavascriptExecutor) driver).executeScript(
                    "var e = document.getElementById('lastName'); if(e){ e.focus(); e.blur(); }");
            ((JavascriptExecutor) driver).executeScript(
                    "if(window.validateField) validateField(document.getElementById('lastName'));");

            // Wait for lastNameError to have visible style and non-empty text
            boolean lastErrOk = wait.until(d -> {
                WebElement el = d.findElement(By.id("lastNameError"));
                String display = (String) ((JavascriptExecutor) d).executeScript(
                        "var e=document.getElementById('lastNameError'); return e? window.getComputedStyle(e).display : 'none';");
                String inner = (String) ((JavascriptExecutor) d).executeScript(
                        "var e=document.getElementById('lastNameError'); return e? (e.innerText||e.textContent||'') : '';");
                boolean visible = display != null && !"none".equals(display);
                boolean hasText = inner != null && inner.trim().length() > 0;
                System.out.println("lastNameError => display:" + display + " | text:'" + inner + "'");
                return visible && hasText;
            });

            if (!lastErrOk) {
                saveDiagnostic("no-lastname-text.png", "page-source-no-lastname-text.html");
                Assertions.fail("lastNameError did not become visible with text.");
            }

            // Final success screenshot: overwrite if exists
            Path out = Path.of(System.getProperty("user.dir")).resolve("error-state.png");
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                Files.copy(src.toPath(), out, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Negative test succeeded. Screenshot: " + out.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Failed to save screenshot: " + e.getMessage());
                // still continue to fail to show issue, but don't crash with FileAlreadyExistsException
                Assertions.fail("Could not save error screenshot: " + e.getMessage());
            }

        } catch (Exception ex) {
            System.err.println("TEST EXCEPTION: " + ex.getClass().getName() + " : " + ex.getMessage());
            // Save diagnostics (overwriting previous diagnostic files if needed)
            try {
                saveDiagnostic("error-state-failure.png", "page-source-failure.html");
            } catch (Exception e) {
                System.err.println("Could not save extra diagnostics: " + e.getMessage());
            }
            throw ex;
        } finally {
            if (driver != null) driver.quit();
        }
    }

    private void saveDiagnostic(String screenshotName, String pageSourceName) {
        try {
            Path base = Path.of(System.getProperty("user.dir"));
            Path out = base.resolve(screenshotName);
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), out, StandardCopyOption.REPLACE_EXISTING);
            System.err.println("Saved diagnostic screenshot: " + out.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Could not save diagnostic screenshot: " + e.getMessage());
        }
        try {
            Path base = Path.of(System.getProperty("user.dir"));
            Path srcHtml = base.resolve(pageSourceName);
            String pageSource = driver.getPageSource();
            Files.writeString(srcHtml, pageSource);
            System.err.println("Saved page source: " + srcHtml.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Could not save page source: " + e.getMessage());
        }
    }
}
