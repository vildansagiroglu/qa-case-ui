package com.hepsiburada.ui.pages;

import com.hepsiburada.ui.utils.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.time.Duration;

public class HomePage {

    private final WebDriver driver;
    private final WebDriverWait wait;


    private final By searchInput = By.cssSelector("input[data-test-id='search-bar-input']");

    // Cookie consent button
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

        // Wait until the search input is in the DOM
        wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));
    }


    public void search(String keyword) {
        acceptCookiesIfPresent();

        int attempts = 0;
        while (attempts < 5) {
            try {
                waitForPageReady();

                // Re-locate the input on every attempt to avoid StaleElementReferenceException
                WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));

                // The input is re-rendered by React, so set the value and fire events via JS
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

                // Did we land on the results page?
                wait.until(d -> {
                    String url = d.getCurrentUrl().toLowerCase();
                    return url.contains("ara") || url.contains("search");
                });

                return; // success

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

            // Short pause so the cookie overlay disappears
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