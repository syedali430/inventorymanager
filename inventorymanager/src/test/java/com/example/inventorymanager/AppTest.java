package com.example.inventorymanager;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppTest {

    @Test
    public void testAppMainPrintsHelloWorld() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            App.main(new String[]{});
            String output = out.toString().trim();
            assertEquals("Hello World!", output);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testAppClassLoads() {
        assertTrue(new App() instanceof App);
    }
}

