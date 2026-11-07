# UI Test Automation – Java · Selenium · Cucumber (BDD)

[![UI tests](https://github.com/vildansagiroglu/qa-case-ui/actions/workflows/ui-tests.yml/badge.svg)](https://github.com/vildansagiroglu/qa-case-ui/actions/workflows/ui-tests.yml)

End-to-end UI tests for the product search and review flow of [Hepsiburada](https://www.hepsiburada.com), one of the largest e-commerce sites in Türkiye. Built as a QA case study with **Selenium WebDriver**, **Cucumber (BDD)** and the **Page Object Model**.

## Scenarios

| Scenario | What it verifies |
| --- | --- |
| Search and open a product (`iphone`, `laptop`) | Search returns results, a random product from the first 10 opens, and the detail page shows the same product |
| Sort and rate reviews | The reviews tab opens, reviews sort by newest, a random review can be rated and a "thank you" message appears |

Feature file: [`src/test/resources/features/ui_scenario1.feature`](src/test/resources/features/ui_scenario1.feature)

## How it handles a real, dynamic website

The target is a live production site, so the framework is built for an unstable DOM:

- **Resilient locators** – `data-test-id` and ARIA attributes first, text-based XPath fallbacks where CSS classes are generated.
- **Explicit waits and retries** – no fixed waits for page state; search retries when React re-renders the input (`StaleElementReferenceException`).
- **Pop-up handling** – cookie banner, live chat and browser permission prompts are closed automatically.
- **Lazy-loaded content** – the reviews tab is found by scrolling step by step until it renders.
- **Data-aware skipping** – a random product may have no reviews; those steps are skipped instead of failing.
- **Debuggable failures** – a screenshot is attached to the Cucumber report when a scenario fails, and the browser is always closed.
- **Thread-safe driver** – one WebDriver per thread (`ThreadLocal`), ready for parallel runs.

## Project structure

```
src/test/java/com/hepsiburada/ui
├── pages/      HomePage, SearchResultsPage, ProductDetailPage (Page Object Model)
├── steps/      UiSteps – Cucumber step definitions with assertions
├── hooks/      Hooks – driver lifecycle and failure screenshots
├── runner/     TestRunner – Cucumber + JUnit runner
└── utils/      DriverFactory – Chrome setup, headless mode
src/test/resources/features   Gherkin feature files
.github/workflows/ui-tests.yml   CI pipeline
```

## Running locally

Requirements: Java 21, Maven 3.9+ and Google Chrome (the driver is downloaded automatically by WebDriverManager).

```bash
mvn test                    # visible browser
mvn test -Dheadless=true    # headless, as in CI
```

The HTML report is written to `target/cucumber-report.html`.

## CI

GitHub Actions compiles the project on every push and pull request. The full browser run is started manually (**Actions → UI tests → Run workflow**), because the live website may block traffic from cloud runners; the Cucumber report is uploaded as an artifact.

## Tech stack

Java 21 · Selenium 4 · Cucumber 7 · JUnit 4 · WebDriverManager · Maven · GitHub Actions

## Author

**Vildan Sağıroğlu** – QA Automation Engineer · [LinkedIn](https://www.linkedin.com/in/vildansagiroglu)
