Feature: Product search and reviews on Hepsiburada

  Background:
    Given user visits hepsiburada homepage

  Scenario Outline: Search returns products and the selected product opens correctly
    When user searches for "<keyword>"
    And user selects a random product
    Then product detail page should be displayed
    And selected product should match detail page

    Examples:
      | keyword |
      | iphone  |
      | laptop  |

  Scenario: User sorts reviews by newest and rates a review
    When user searches for "iphone"
    And user selects a random product
    And user opens the reviews tab
    And user sorts reviews by newest
    And user rates a random review as helpful or not helpful
    Then a thank you message should be displayed
