package com.hepsiburada.ui.pages;

import com.hepsiburada.ui.utils.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class ProductDetailPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Ürün başlığı
    private final By productTitle = By.cssSelector("h1");

    // DEĞERLENDİRMELER TAB
    private final By reviewsTabPrimary = By.cssSelector("button[role='tab'][aria-controls='Reviews']");

    // Fallback: aria-label içinde Değerlendirmeler
    private final By reviewsTabFallback = By.xpath(
            "//button[@role='tab' and (contains(@aria-label,'Değerlendirm') or contains(normalize-space(.),'Değerlendirm'))]"
    );

    // Tab bar (varsa)
    private final By tabList = By.cssSelector("[role='tablist']");

    // SIRALA DROPDOWN
    // “Sırala” alanındaki “Varsayılan” (class’lar değişken olduğu için text bazlı)
    private final By sortDropdown = By.xpath(
            "//*[normalize-space()='Sırala']/following::div[normalize-space()='Varsayılan'][1]"
    );

    private final By newestOption = By.xpath("//*[normalize-space()='En yeni değerlendirme']");

    // THUMB BUTONLARI
    private final By thumbButtons = By.cssSelector("div.thumbsUp, div.thumbsDown");

    private final By thankYouText = By.xpath(
            "//*[normalize-space(.)='Teşekkür Ederiz.' or normalize-space(.)='Teşekkür ederiz.']"
    );

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

        // Sayfada “Değerlendirmeler” tab’ı genelde aşağıda.
        // Bulana kadar kontrollü scroll yap
        for (int i = 0; i < 14; i++) { // 14 * ~600px = bayağı aşağı
            closeLiveChatIfPresent();

            WebElement tab = findReviewsTabIfPresent();
            if (tab != null) {
                scrollIntoViewCenter(tab);
                closeLiveChatIfPresent();
                clickSafe(tab);

                // tıkladıktan sonra Reviews içeriğinin gelmesini bekle
                wait.until(d ->
                        d.findElements(sortDropdown).size() > 0 ||
                                d.findElements(thumbButtons).size() > 0 ||
                                d.getPageSource().toLowerCase().contains("değerlendirme")
                );
                return;
            }

            // Bulamadıysak aşağı kaydır
            jsScrollBy(0, 650);
            waitShort(250);
        }

        throw new TimeoutException("Değerlendirmeler tabı bulunamadı (scroll-loop sonrası).");
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
        closeLiveChatIfPresent();

        WebElement dd = wait.until(ExpectedConditions.presenceOfElementLocated(sortDropdown));
        scrollIntoViewCenter(dd);
        clickSafe(dd);

        WebElement newest = wait.until(ExpectedConditions.elementToBeClickable(newestOption));
        clickSafe(newest);

        waitShort(700);
    }

    public void clickRandomThumb() {
        closeLiveChatIfPresent();

        List<WebElement> thumbs = driver.findElements(thumbButtons);
        if (thumbs.isEmpty()) return;

        int limit = Math.min(thumbs.size(), 10);
        WebElement btn = thumbs.get(new Random().nextInt(limit));

        scrollIntoViewCenter(btn);
        clickSafe(btn);

        // Teşekkür yazısını bekle ve ekranda göster
        try {
            WebElement thankYou = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(thankYouText));
            scrollIntoViewCenter(thankYou);
            waitShort(3000);
        } catch (Exception e) {
            waitShort(2000);
        }
    }

    public boolean isThankYouMessageDisplayed() {
        try {
            return driver.findElements(thankYouText).size() > 0;
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
        // header vs yüklenme anında click’ler boşa gidiyor, kısa stabilize
        waitShort(800);
    }

    private void waitShort(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}