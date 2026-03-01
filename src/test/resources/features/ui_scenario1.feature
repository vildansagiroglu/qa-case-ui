Feature: Hepsiburada UI Test

  Scenario: Search and open random product
    Given user visits hepsiburada homepage
    When user searches for "iphone"
    And user selects a random product
    Then product detail page should be displayed
    And selected product should match detail page
    When user goes to reviews tab and sorts by newest