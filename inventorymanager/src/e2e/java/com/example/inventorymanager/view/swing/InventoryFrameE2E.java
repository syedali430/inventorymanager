package com.example.inventorymanager.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import com.mongodb.MongoClient;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.awaitility.Awaitility;
import org.bson.Document;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MongoDBContainer;

@RunWith(GUITestRunner.class)
public class InventoryFrameE2E extends AssertJSwingJUnitTestCase {

    @ClassRule
    public static final MongoDBContainer mongo = new MongoDBContainer("mongo:4.4.3");

    private static final String DB_NAME = "test-db";
    private static final String COLLECTION_NAME = "test-collection";

    private static final String FIXTURE_1_ID = "1";
    private static final String FIXTURE_1_NAME = "Monitor";
    private static final int FIXTURE_1_QUANTITY = 2;
    private static final double FIXTURE_1_PRICE = 199.99;
    private static final String FIXTURE_1_DESC = "HD monitor";

    private static final String FIXTURE_2_ID = "2";
    private static final String FIXTURE_2_NAME = "Keyboard";
    private static final int FIXTURE_2_QUANTITY = 3;
    private static final double FIXTURE_2_PRICE = 49.99;
    private static final String FIXTURE_2_DESC = "Mechanical";

    private MongoClient mongoClient;
    private FrameFixture window;
    private String containerHost;
    private int containerPort;

    @Override
    protected void onSetUp() {
        containerHost = mongo.getContainerIpAddress();
        containerPort = mongo.getFirstMappedPort();
        mongoClient = new MongoClient(containerHost, containerPort);
        mongoClient.getDatabase(DB_NAME).drop();
        addTestItemToDatabase(FIXTURE_1_ID, FIXTURE_1_NAME, FIXTURE_1_QUANTITY, FIXTURE_1_PRICE, FIXTURE_1_DESC);
        addTestItemToDatabase(FIXTURE_2_ID, FIXTURE_2_NAME, FIXTURE_2_QUANTITY, FIXTURE_2_PRICE, FIXTURE_2_DESC);

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
        }).using(robot());
    }

    @Override
    protected void onTearDown() {
        if (window != null) {
            window.cleanUp();
        }
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    private void addTestItemToDatabase(String id, String name, int quantity, double price, String description) {
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

    @Test
    @GUITest
    public void testOnStartAllDatabaseElementsAreShown() {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String[] contents = window.list().contents();
            assertThat(contents)
                .anySatisfy(e -> assertThat(e).contains(FIXTURE_1_NAME, String.valueOf(FIXTURE_1_QUANTITY),
                    String.valueOf(FIXTURE_1_PRICE)))
                .anySatisfy(e -> assertThat(e).contains(FIXTURE_2_NAME, String.valueOf(FIXTURE_2_QUANTITY),
                    String.valueOf(FIXTURE_2_PRICE)));
        });
    }

    @Test
    @GUITest
    public void testAddItemE2E() {
        window.textBox("nameField").setText("Laptop");
        window.textBox("quantityField").setText("10");
        window.textBox("priceField").setText("999.99");
        window.textBox("descField").setText("Gaming Laptop");
        window.button("addButton").click();

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String[] listContents = window.list().contents();
            assertThat(listContents).anySatisfy(e -> assertThat(e).contains("Laptop", "10", "999.99"));
        });
    }
}
