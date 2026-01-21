package com.example.inventorymanager;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class AppTest {

    @Test
    public void testMainDoesNotWriteToStdout() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            App.main(new String[]{});
        } finally {
            System.setOut(originalOut);
        }
        assertEquals("", captured.toString());
    }
}
