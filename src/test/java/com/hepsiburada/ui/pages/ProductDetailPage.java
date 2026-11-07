package com.hepsiburada.ui.pages;

import com.hepsiburada.ui.utils.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class ProductDetailPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Product title
    private final By productTitle = By.cssSelector("h1");

    // Reviews tab ("Değerlendirmeler")
    private final By reviewsTabPrimary = By.cssSelector("button[role='tab'][aria-controls='Reviews']");

    // Fallback: match the tab by its visible text or aria-label
    private final By reviewsTabFallback = By.xpath(
            "//button[@role='tab' and (contains(@aria-label,'Değerlendirm') or contains(normalize-space(.),'Değerlendirm'))]"
    );

    // Tab bar (if present)
    private final By tabList = By.cssSelector("[role='tablist']");

    // Sort dropdown: located by text because CSS class names are generated and change
    private final By sortDropdown = By.xpath(
            "//*[normalize-space()='Sırala']/following::div[normalize-space()='Varsayılan'][1]"
    );

    // Lower-cases text in XPath 1.0 so "En yeni" and "En Yeni" both match
    private static final String LOWER =
            "translate(normalize-space(.),'ABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ','abcçdefgğhiijklmnoöprsştuüvyz')";

    // Deepest element whose text contains "En yeni" (newest first), robust to small label changes
    private final By newestOption = By.xpath(
            "//*[contains(" + LOWER + ",'en yeni') and not(*[contains(" + LOWER + ",'en yeni')])]"
    );

    // Cookie consent banner: OneTrust id first, text-based fallback
    private final By acceptCookiesById = By.id("onetrust-accept-btn-handler");
    private final By acceptCookiesByText = By.xpath("//button[contains(normalize-space(.),'Kabul Et')]");

    // Helpful / not helpful buttons
    private final By thumbButtons = By.cssSelector("div.thumbsUp, div.thumbsDown");

    // "Teşekkür ederiz" confirmation, any casing, with or without a trailing dot
    private final By thankYouText = By.xpath(
            "//*[contains(" + LOWER + ",'teşekkür') and not(*[contains(" + LOWER + ",'teşekkür')])]"
    );

    private boolean thankYouSeen;

    public ProductDetailPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public boolean isProductPageLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(productTitle));
            return !driver.findElement(productTitle).getText().trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSameProduct(String selectedProductName) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(productTitle));
            String detailTitle = driver.findElement(productTitle).getText().trim().toLowerCase();
            if (selectedProductName == null) return false;
            String listTitle = selectedProductName.trim().toLowerCase();
            return detailTitle.contains(listTitle) || listTitle.contains(detailTitle);
        } catch (Exception e) {
            return false;
        }
    }


    public void goToReviewsTab() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(productTitle));
        waitForPageToSettle();
        // The product opens in a new tab where the cookie banner can cover the reviews controls
        acceptCookiesIfPresent();

        // The reviews tab is usually far down the page and lazy-loaded,
        // so scroll step by step until it appears (max ~14 x 650px)
        for (int i = 0; i < 14; i++) {
            closeLiveChatIfPresent();

            WebElement tab = findReviewsTabIfPresent();
            if (tab != null) {
                scrollIntoViewCenter(tab);
                closeLiveChatIfPresent();
                clickSafe(tab);

                // Wait until the reviews content is rendered
                wait.until(d ->
                        d.findElements(sortDropdown).size() > 0 ||
                                d.findElements(thumbButtons).size() > 0 ||
                                d.getPageSource().toLowerCase().contains("değerlendirme")
                );
                return;
            }

            // Not found yet: scroll further down
            jsScrollBy(0, 650);
            waitShort(250);
        }

        throw new TimeoutException("Reviews tab not found after scrolling the page");
    }

    public boolean hasReviews() {
        try {
            closeLiveChatIfPresent();
            return driver.findElements(sortDropdown).size() > 0 || driver.findElements(thumbButtons).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void sortByNewest() {
        acceptCookiesIfPresent();
        closeLiveChatIfPresent();

        WebElement dd = wait.until(ExpectedConditions.presenceOfElementLocated(sortDropdown));
        scrollIntoViewCenter(dd);

        // Open the dropdown; retry with a real mouse click if the first click did not open it
        clickSafe(dd);
        if (!isVisibleWithin(newestOption, 4)) {
            try {
                new Actions(driver).moveToElement(dd).click().perform();
            } catch (Exception ignored) {
            }
        }

        WebElement newest = wait.until(ExpectedConditions.visibilityOfElementLocated(newestOption));
        clickSafe(newest);

        waitShort(700);
    }

    private void acceptCookiesIfPresent() {
        for (By by : new By[]{acceptCookiesById, acceptCookiesByText}) {
            List<WebElement> buttons = driver.findElements(by);
            if (!buttons.isEmpty() && buttons.get(0).isDisplayed()) {
                clickSafe(buttons.get(0));
                waitShort(300);
                return;
            }
        }
    }

    private boolean isVisibleWithin(By locator, int seconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(seconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void clickRandomThumb() {
        closeLiveChatIfPresent();

        List<WebElement> thumbs = driver.findElements(thumbButtons);
        if (thumbs.isEmpty()) return;

        int limit = Math.min(thumbs.size(), 10);
        WebElement btn = thumbs.get(new Random().nextInt(limit));

        scrollIntoViewCenter(btn);
        clickSafe(btn);

        // The "thank you" toast disappears after a moment, so capture it right after the click
        // with fast polling and remember the result for the Then step
        thankYouSeen = false;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8), Duration.ofMillis(100))
                    .until(ExpectedConditions.visibilityOfElementLocated(thankYouText));
            thankYouSeen = true;
        } catch (TimeoutException ignored) {
            // isThankYouMessageDisplayed() reports the failure
        }
    }

    /** True if the confirmation appeared after the last rating click (it is a short-lived toast). */
    public boolean isThankYouMessageDisplayed() {
        if (thankYouSeen) {
            return true;
        }
        try {
            return driver.findElements(thankYouText).stream().anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    // HELPERS

    private WebElement findReviewsTabIfPresent() {
        List<WebElement> p = driver.findElements(reviewsTabPrimary);
        if (!p.isEmpty()) return p.get(0);

        List<WebElement> f = driver.findElements(reviewsTabFallback);
        if (!f.isEmpty()) return f.get(0);


        return null;
    }

    private void closeLiveChatIfPresent() {

        By[] candidates = new By[] {
                By.cssSelector("button[aria-label*='kapat' i]"),
                By.cssSelector("button[aria-label*='close' i]"),
                By.cssSelector("button[aria-label*='popup' i]"),
                By.cssSelector("[data-testid*='close' i]"),
                By.cssSelector("[data-test-id*='close' i]"),
        };

        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(1));

        for (By by : candidates) {
            try {
                List<WebElement> els = driver.findElements(by);
                if (els.isEmpty()) continue;

                WebElement btn = shortWait.until(ExpectedConditions.elementToBeClickable(els.get(0)));
                try {
                    btn.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                }
                waitShort(150);
                return;
            } catch (Exception ignored) {
            }
        }
    }

    private void clickSafe(WebElement el) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(el)).click();
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            } catch (Exception ignored) {}
        }
    }

    private void scrollIntoViewCenter(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'center'});", el);
            waitShort(200);
        } catch (Exception ignored) {}
    }

    private void jsScrollBy(int x, int y) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
    }

    private void waitForPageToSettle() {
        // Clicks are lost while the header is still loading: give the page a short moment
        waitShort(800);
    }

    private void waitShort(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}