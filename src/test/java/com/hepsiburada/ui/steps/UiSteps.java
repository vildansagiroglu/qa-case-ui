package com.hepsiburada.ui.steps;

import com.hepsiburada.ui.pages.HomePage;
import com.hepsiburada.ui.pages.ProductDetailPage;
import com.hepsiburada.ui.pages.SearchResultsPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Given;

import static org.junit.Assert.assertTrue;

public class UiSteps {

    HomePage homePage = new HomePage();
    SearchResultsPage resultsPage = new SearchResultsPage();
    ProductDetailPage detailPage = new ProductDetailPage();

    String selectedProductName;

    @Given("user visits hepsiburada homepage")
    public void userVisitsHepsiburadaHomepage() {
        homePage.goToHomepage();
    }

    @When("user searches for {string}")
    public void userSearchesFor(String keyword) {
        homePage.search(keyword);
    }

    @When("user selects a random product")
    public void userSelectsRandomProduct() {
        selectedProductName = resultsPage.selectRandomProduct();
    }

    @Then("product detail page should be displayed")
    public void productDetailPageShouldBeDisplayed() {
        assertTrue(detailPage.isProductPageLoaded());
    }

    @Then("selected product should match detail page")
    public void selectedProductShouldMatchDetailPage() {

    }

    @When("user goes to reviews tab and sorts by newest")
    public void userGoesToReviewsTab() {

        detailPage.goToReviewsTab();

        if (detailPage.hasReviews()) {
            detailPage.sortByNewest();
            detailPage.clickRandomThumb();

        }
        // Eğer yorum yoksa hiçbir şey yapılmaz (requirement'a uygun)
    }
}