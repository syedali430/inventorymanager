package com.example.inventorymanager.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

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
import org.testcontainers.containers.MongoDBContainer;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ItemDescriptionSteps {

    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:4.4.3");
    private static final String DB_NAME = "bdd-db";
    private static final String COLLECTION_NAME = "bdd-collection";

    private MongoClient mongoClient;
    private Robot robot;
    private FrameFixture window;
    private String containerHost;
    private int containerPort;

    @Before
    public void setUp() {
        if (!mongo.isRunning()) {
            mongo.start();
        }
        containerHost = mongo.getContainerIpAddress();
        containerPort = mongo.getFirstMappedPort();
        mongoClient = new MongoClient(containerHost, containerPort);
        mongoClient.getDatabase(DB_NAME).drop();
        robot = BasicRobot.robotWithNewAwtHierarchy();
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

    @Given("the database contains an item with id {string} name {string} quantity {int} price {double} description {string}")
    public void theDatabaseContainsAnItem(String id, String name, int quantity, double price, String description) {
        mongoClient
            .getDatabase(DB_NAME)
            .getCollection(COLLECTION_NAME)
            .insertOne(new Document()
                .append("id", id)
                .append("name", name)
                .append("quantity", quantity)
                .append("price", price)
                .append("description", description));
    }

    @When("the Inventory Manager is shown")
    public void theInventoryManagerIsShown() {
        application("com.example.inventorymanager.app.InventoryApplication")
            .withArgs(
                "--mongo-host=" + containerHost,
                "--mongo-port=" + containerPort,
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

    @Then("the list contains an element with name {string} quantity {int} price {double}")
    public void theListContainsAnElement(String name, int quantity, double price) {
        String quantityText = String.valueOf(quantity);
        String priceText = String.valueOf(price);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.list().contents())
                .anySatisfy(e -> assertThat(e).contains(name, quantityText, priceText));
        });
    }
}
