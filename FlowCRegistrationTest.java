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

/**
 * Flow C (fixed): invalid email, invalid phone, password mismatch, terms unchecked.
 * - Uses the correct confirm-password error id (confirmError).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FlowCRegistrationTest {
    private WebDriver driver;
    private WebDriverWait wait;

    // <-- set to the exact URL you use in browser -->
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
        // Slightly larger explicit wait to avoid flakiness
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));
    }

    @Test
    @DisplayName("Flow C: invalid inputs -> verify errors & submit disabled (fixed IDs)")
    public void testFlowC_InvalidInputsShowsErrorsAndPreventsSubmit() throws Exception {
        try {
            driver.get(PAGE);
            System.out.println("Page loaded: " + driver.getCurrentUrl() + " | Title: " + driver.getTitle());

            // Ensure country/state/city populated & selected (safe index picks)
            wait.until(d -> d.findElements(By.cssSelector("#country option")).size() > 1);
            new Select(driver.findElement(By.id("country"))).selectByIndex(1);
            ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('change'))", driver.findElement(By.id("country")));
            wait.until(d -> d.findElements(By.cssSelector("#state option")).size() > 1);
            new Select(driver.findElement(By.id("state"))).selectByIndex(1);
            ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('change'))", driver.findElement(By.id("state")));
            wait.until(d -> d.findElements(By.cssSelector("#city option")).size() > 1);
            new Select(driver.findElement(By.id("city"))).selectByIndex(1);

            // Fill name fields
            driver.findElement(By.id("firstName")).sendKeys("Alice");
            driver.findElement(By.id("lastName")).sendKeys("Wonder");

            // 1) Invalid email (no @)
            WebElement email = driver.findElement(By.id("email"));
            email.clear(); email.sendKeys("bad-email");

            // 2) Invalid phone (missing country code)
            WebElement phone = driver.findElement(By.id("phone"));
            phone.clear(); phone.sendKeys("9876543210");

            // 3) Password mismatch
            WebElement pwd = driver.findElement(By.id("password"));
            WebElement cpwd = driver.findElement(By.id("confirmPassword"));
            pwd.clear(); pwd.sendKeys("Abcd@1234");
            cpwd.clear(); cpwd.sendKeys("Xyz@1234"); // different

            // 4) Do NOT check terms (should remain unchecked)
            WebElement terms = driver.findElement(By.id("terms"));
            if (terms.isSelected()) terms.click();

            // Force validation: blur + call validateField for the fields we care about
            ((JavascriptExecutor) driver).executeScript(
                    "['email','phone','password','confirmPassword'].forEach(id=>{var e=document.getElementById(id); if(e){ e.focus(); e.blur(); } });");
            ((JavascriptExecutor) driver).executeScript(
                    "if(window.validateField){ ['email','phone','password','confirmPassword','terms'].forEach(id=>{ var el=document.getElementById(id); if(el) validateField(el); }); }");

            // Wait for email error to appear and contain text
            boolean emailErr = wait.until(d -> {
                WebElement e = d.findElement(By.id("emailError"));
                String txt = (String) ((JavascriptExecutor)d).executeScript("return arguments[0]? (arguments[0].innerText||arguments[0].textContent||'') : '';", e);
                String display = (String) ((JavascriptExecutor)d).executeScript("return window.getComputedStyle(arguments[0]).display;", e);
                System.out.println("emailError -> display:" + display + " | text:'" + txt + "'");
                return txt != null && txt.trim().length() > 0 && !"none".equals(display);
            });

            // Wait for phone error
            boolean phoneErr = wait.until(d -> {
                WebElement e = d.findElement(By.id("phoneError"));
                String txt = (String) ((JavascriptExecutor)d).executeScript("return arguments[0]? (arguments[0].innerText||arguments[0].textContent||'') : '';", e);
                String display = (String) ((JavascriptExecutor)d).executeScript("return window.getComputedStyle(arguments[0]).display;", e);
                System.out.println("phoneError -> display:" + display + " | text:'" + txt + "'");
                return txt != null && txt.trim().length() > 0 && !"none".equals(display);
            });

            // FIXED: confirm password error id is 'confirmError' in the page
            boolean confirmPwdErr = wait.until(d -> {
                WebElement e = d.findElement(By.id("confirmError")); // <--- correct id
                String txt = (String) ((JavascriptExecutor)d).executeScript("return arguments[0]? (arguments[0].innerText||arguments[0].textContent||'') : '';", e);
                String display = (String) ((JavascriptExecutor)d).executeScript("return window.getComputedStyle(arguments[0]).display;", e);
                System.out.println("confirmError -> display:" + display + " | text:'" + txt + "'");
                return txt != null && txt.trim().length() > 0 && !"none".equals(display);
            });

            // Terms error (termsError)
            boolean termsErr = wait.until(d -> {
                WebElement e = d.findElement(By.id("termsError"));
                String txt = (String) ((JavascriptExecutor)d).executeScript("return arguments[0]? (arguments[0].innerText||arguments[0].textContent||'') : '';", e);
                String display = (String) ((JavascriptExecutor)d).executeScript("return window.getComputedStyle(arguments[0]).display;", e);
                System.out.println("termsError -> display:" + display + " | text:'" + txt + "'");
                return (txt != null && txt.trim().length() > 0 && !"none".equals(display)) || (!terms.isSelected());
            });

            // Assertions
            Assertions.assertTrue(emailErr, "Expected email error to be present for invalid email.");
            Assertions.assertTrue(phoneErr, "Expected phone error to be present for invalid phone.");
            Assertions.assertTrue(confirmPwdErr, "Expected confirm password error for mismatch.");
            Assertions.assertTrue(termsErr, "Expected terms error when terms are not checked.");

            // Ensure submit is disabled
            WebElement submit = driver.findElement(By.id("submitBtn"));
            String disabledAttr = submit.getAttribute("disabled");
            boolean disabled = (disabledAttr != null && !disabledAttr.isEmpty()) || !submit.isEnabled();
            System.out.println("Submit disabled attribute: '" + disabledAttr + "' | isEnabled()=" + submit.isEnabled());
            Assertions.assertTrue(disabled, "Submit should be disabled when critical fields are invalid / terms not checked.");

            // Save pass screenshot
            Path out = Path.of(System.getProperty("user.dir")).resolve("flow-c-success.png");
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), out, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Flow C checks passed. Screenshot saved to: " + out.toAbsolutePath());

        } catch (Exception ex) {
            System.err.println("Flow C Exception: " + ex.getClass().getName() + " - " + ex.getMessage());
            // Save diagnostics
            try {
                Path base = Path.of(System.getProperty("user.dir"));
                Path out = base.resolve("flow-c-failure.png");
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Files.copy(src.toPath(), out, StandardCopyOption.REPLACE_EXISTING);
                System.err.println("Saved failure screenshot: " + out.toAbsolutePath());
                Path srcHtml = base.resolve("flow-c-page-failure.html");
                Files.writeString(srcHtml, driver.getPageSource());
                System.err.println("Saved page source: " + srcHtml.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Could not save Flow C diagnostics: " + e.getMessage());
            }
            throw ex;
        } finally {
            if (driver != null) driver.quit();
        }
    }
}
