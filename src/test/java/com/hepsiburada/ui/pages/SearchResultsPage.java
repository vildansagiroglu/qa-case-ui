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

public class SearchResultsPage {

     WebDriver driver = DriverFactory.getDriver();
     WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

    private By productList = By.cssSelector("li[class*='productListContent'] a");

    public String selectRandomProduct() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(productList));
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(productList, 0));

        // Ürünleri array'e al
        List<WebElement> productArray = driver.findElements(productList);

        // Random seç (ilk 10'dan)
        int randomIndex = new Random().nextInt(Math.min(productArray.size(), 10));

        // Seçilen ürünün adını al — detay sayfasında karşılaştırılacak
        String selectedProductName = productArray.get(randomIndex).getAttribute("title");
        if (selectedProductName == null || selectedProductName.isEmpty()) {
            selectedProductName = productArray.get(randomIndex).getText();
        }

        // Taze referansla tıkla
        List<WebElement> freshArray = driver.findElements(productList);
        WebElement target = freshArray.get(randomIndex);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", target);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);

        return selectedProductName;
    }
}