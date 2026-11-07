package com.hepsiburada.ui.pages;

import com.hepsiburada.ui.utils.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SearchResultsPage {

    private final WebDriver driver = DriverFactory.getDriver();
    private final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

    private final By productList = By.cssSelector("li[class*='productListContent'] a");

    public String selectRandomProduct() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(productList));
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(productList, 0));

        // Collect the product cards
        List<WebElement> productArray = driver.findElements(productList);

        // Pick a random product from the first 10 results
        int randomIndex = new Random().nextInt(Math.min(productArray.size(), 10));

        // Keep the product name to compare it on the detail page
        String selectedProductName = productArray.get(randomIndex).getAttribute("title");
        if (selectedProductName == null || selectedProductName.isEmpty()) {
            selectedProductName = productArray.get(randomIndex).getText();
        }


        List<WebElement> freshArray = driver.findElements(productList);
        WebElement target = freshArray.get(randomIndex);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", target);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        Set<String> handlesBefore = driver.getWindowHandles();
        String resultsUrl = driver.getCurrentUrl();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);

        switchToProductTab(handlesBefore, resultsUrl);
        return selectedProductName;
    }

    /**
     * Hepsiburada opens a product in a new browser tab. Selenium stays on the old tab
     * unless we switch, so wait for either a new tab or a navigation in the same tab.
     */
    private void switchToProductTab(Set<String> handlesBefore, String resultsUrl) {
        wait.until(d -> d.getWindowHandles().size() > handlesBefore.size()
                || !d.getCurrentUrl().equals(resultsUrl));

        for (String handle : driver.getWindowHandles()) {
            if (!handlesBefore.contains(handle)) {
                driver.switchTo().window(handle);
                return;
            }
        }
    }
}