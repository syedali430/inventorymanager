package com.example.inventorymanager.bdd;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/bdd/resources",
    glue = "com.example.inventorymanager.bdd",
    monochrome = true
)
public class ItemBDD {
}
