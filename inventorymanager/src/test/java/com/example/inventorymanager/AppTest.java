package com.example.inventorymanager;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AppTest {

    @Test
    public void testAppMainRunsWithoutException() {
        App.main(new String[]{});
        assertTrue(true);
    }

    @Test
    public void testAppClassLoads() {
        assertTrue(new App() instanceof App);
    }
}

