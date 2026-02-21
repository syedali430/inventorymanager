package com.example.inventorymanager.bdd;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import java.net.InetSocketAddress;


@RunWith(Cucumber.class)
@CucumberOptions(features = "src/bdd/resources", monochrome = true)
public class InventorySwingAppBDD {

    private static MongoServer mongoServer;
    private static InetSocketAddress serverAddress;

    public static int mongoPort;

    @BeforeClass
    public static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();

        String portProperty = System.getProperty("mongo.port", "").trim();
        Integer parsedPort = parsePort(portProperty);
        if (parsedPort == null) {
            mongoServer = new MongoServer(new MemoryBackend());
            serverAddress = mongoServer.bind();
            mongoPort = serverAddress.getPort();
            System.setProperty("mongo.port", String.valueOf(mongoPort));
        } else {
            mongoPort = parsedPort;
        }
    }

    @AfterClass
    public static void tearDownOnce() {
        if (mongoServer != null) {
            mongoServer.shutdownNow();
        }
    }

    private static Integer parsePort(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
