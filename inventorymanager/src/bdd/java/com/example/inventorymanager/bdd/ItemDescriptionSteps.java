package com.example.inventorymanager.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import com.mongodb.MongoClient;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.awaitility.Awaitility;
import org.bson.Document;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ItemDescriptionSteps {

    private static final String DB_NAME = "test-db";
    private static final String COLLECTION_NAME = "test-collection";

    private MongoClient mongoClient;
    private Robot robot;
    private FrameFixture window;

    @Before
    public void setUp() {
        mongoClient = new MongoClient(resolveHost(), resolvePort());
        mongoClient.getDatabase(DB_NAME).drop();
        robot = BasicRobot.robotWithCurrentAwtHierarchy();
    }

    @After
    public void tearDown() {
        if (window != null) {
            window.cleanUp();
        }
        if (robot != null) {
            robot.cleanUp();
        }
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Given("the database contains the items with the following values")
    public void theDatabaseContainsItemsWithValues(List<List<String>> values) {
        values.forEach(row -> {
            String id = row.get(0).trim();
            String name = row.get(1).trim();
            int quantity = Integer.parseInt(row.get(2).trim());
            double price = Double.parseDouble(row.get(3).trim());
            String description = row.get(4).trim();
            mongoClient
                .getDatabase(DB_NAME)
                .getCollection(COLLECTION_NAME)
                .insertOne(new Document()
                    .append("id", id)
                    .append("name", name)
                    .append("quantity", quantity)
                    .append("price", price)
                    .append("description", description));
        });
    }

    @When("the Inventory Manager is shown")
    public void theInventoryManagerIsShown() {
        application("com.example.inventorymanager.app.InventoryApplication")
            .withArgs(
                "--mongo-host=" + resolveHost(),
                "--mongo-port=" + resolvePort(),
                "--db-name=" + DB_NAME,
                "--db-collection=" + COLLECTION_NAME
            )
            .start();

        window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
            @Override
            protected boolean isMatching(JFrame frame) {
                return "Inventory Manager".equals(frame.getTitle()) && frame.isShowing();
            }
        }).using(robot);
    }

    @Then("the list contains elements with the following values")
    public void theListContainsElementsWithValues(List<List<String>> values) {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String[] contents = window.list().contents();
            values.forEach(row -> {
                String name = row.get(0).trim();
                String quantity = row.get(1).trim();
                String price = row.get(2).trim();
                assertThat(contents).anySatisfy(e -> assertThat(e).contains(name, quantity, price));
            });
        });
    }

    private static String resolveHost() {
        return System.getProperty("mongo.host", "localhost");
    }

    private static int resolvePort() {
        String port = System.getProperty("mongo.port", "27017");
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return 27017;
        }
    }
}
