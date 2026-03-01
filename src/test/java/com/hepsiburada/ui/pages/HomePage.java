package com.hepsiburada.ui.pages;

import com.hepsiburada.ui.utils.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.time.Duration;

public class HomePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Net: input'un kendisi (sen console'da doğruladın)
    private final By searchInput = By.cssSelector("input[data-test-id='search-bar-input']");

    // Cookie kabul butonu (button içinde yazı arıyoruz)
    private final By acceptCookiesBtn = By.xpath(
            "//button[.//*[contains(normalize-space(.),'Kabul') or contains(normalize-space(.),'Accept')] or " +
                    "contains(normalize-space(.),'Kabul et') or contains(normalize-space(.),'Accept')]"
    );

    public HomePage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public void goToHomepage() {
        driver.get("https://www.hepsiburada.com/");
        waitForPageReady();
        acceptCookiesIfPresent();
        waitForPageReady();

        // input DOM'a gelsin
        wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));
    }

    /**
     * CLICK'e güvenmeden arama:
     * - focus ver
     * - value'yu native setter ile set et
     * - input event dispatch
     * - Enter keydown dispatch
     */
    public void search(String keyword) {
        acceptCookiesIfPresent();

        int attempts = 0;
        while (attempts < 5) {
            try {
                waitForPageReady();

                // Her denemede elementi yeniden bul (stale'i engeller)
                WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));

                // JS ile focus + value set + event dispatch
                ((JavascriptExecutor) driver).executeScript(
                        "const input = arguments[0];" +
                                "input.scrollIntoView({block:'center'});" +
                                "input.focus();" +
                                "const nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
                                "nativeSetter.call(input, arguments[1]);" +
                                "input.dispatchEvent(new Event('input', { bubbles: true }));" +
                                "input.dispatchEvent(new Event('change', { bubbles: true }));" +
                                "input.dispatchEvent(new KeyboardEvent('keydown', {key:'Enter', code:'Enter', keyCode:13, which:13, bubbles:true}));",
                        input, keyword
                );

                // Sonuç sayfasına geçti mi?
                wait.until(d -> {
                    String url = d.getCurrentUrl().toLowerCase();
                    return url.contains("ara") || url.contains("search");
                });

                return; // başarılı

            } catch (StaleElementReferenceException | TimeoutException e) {
                attempts++;
                sleep(600);
            }
        }

        throw new RuntimeException("Search failed after retries. (Input not interactable / DOM keeps refreshing)");
    }



    private void acceptCookiesIfPresent() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement btn = shortWait.until(ExpectedConditions.elementToBeClickable(acceptCookiesBtn));
            btn.click();

            // cookie overlay kalksın diye kısa bekleme
            sleep(300);
        } catch (Exception ignored) {
        }
    }

    private void waitForPageReady() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(25)).until(d ->
                    "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState"))
            );
        } catch (Exception ignored) {}
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}