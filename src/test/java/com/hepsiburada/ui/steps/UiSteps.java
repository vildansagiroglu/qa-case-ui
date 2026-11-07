package com.hepsiburada.ui.steps;

import com.hepsiburada.ui.pages.HomePage;
import com.hepsiburada.ui.pages.ProductDetailPage;
import com.hepsiburada.ui.pages.SearchResultsPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class UiSteps {

    private final HomePage homePage = new HomePage();
    private final SearchResultsPage resultsPage = new SearchResultsPage();
    private final ProductDetailPage detailPage = new ProductDetailPage();

    private String selectedProductName;

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
        assertTrue("Product detail page did not load", detailPage.isProductPageLoaded());
    }

    @Then("selected product should match detail page")
    public void selectedProductShouldMatchDetailPage() {
        assertTrue("Detail page shows a different product than the one selected: " + selectedProductName,
                detailPage.isSameProduct(selectedProductName));
    }

    @When("user opens the reviews tab")
    public void userOpensReviewsTab() {
        detailPage.goToReviewsTab();
        // A random product may have no reviews yet: skip the remaining steps instead of failing
        assumeTrue("Selected product has no reviews, review steps skipped", detailPage.hasReviews());
    }

    @When("user sorts reviews by newest")
    public void userSortsReviewsByNewest() {
        detailPage.sortByNewest();
    }

    @When("user rates a random review as helpful or not helpful")
    public void userRatesRandomReview() {
        detailPage.clickRandomThumb();
    }

    @Then("a thank you message should be displayed")
    public void thankYouMessageShouldBeDisplayed() {
        assertTrue("Thank you message was not shown after rating a review",
                detailPage.isThankYouMessageDisplayed());
    }
}
