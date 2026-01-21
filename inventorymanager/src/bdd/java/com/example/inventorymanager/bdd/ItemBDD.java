package com.example.inventorymanager.bdd;

import org.junit.runner.RunWith;
import org.junit.BeforeClass;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/bdd/resources",
    glue = "com.example.inventorymanager.bdd",
    monochrome = true
)
public class ItemBDD {
    @BeforeClass
    public static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }
}
