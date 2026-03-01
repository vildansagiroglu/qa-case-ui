package com.hepsiburada.ui.hooks;

import com.hepsiburada.ui.utils.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hooks {

    @Before
    public void beforeScenario() {
        DriverFactory.getDriver();
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            // Fail olunca kapatma ki ekranda neye takıldığını görelim
            return;
        }
        DriverFactory.quitDriver();
    }
}