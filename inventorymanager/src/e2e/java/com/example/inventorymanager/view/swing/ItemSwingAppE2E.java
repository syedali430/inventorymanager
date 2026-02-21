package com.example.inventorymanager.view.swing;

import javax.swing.JFrame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.*;

import java.util.concurrent.TimeUnit;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.awaitility.Awaitility;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import java.net.InetSocketAddress;

//1Laptop

@RunWith(GUITestRunner.class)
public class ItemSwingAppE2E extends AssertJSwingJUnitTestCase {

    private static MongoServer mongoServer;
    private static InetSocketAddress serverAddress;

    private static final String DB_NAME = "test-db";
    private static final String COLLECTION_NAME = "test-collection";


    private static final String ITEM_FIXTURE_1_ID = "1";
    private static final String ITEM_FIXTURE_1_NAME = "Laptop";
    private static final int ITEM_FIXTURE_1_QUALITY = 10;
    private static final double ITEM_FIXTURE_1_PRICE = 999.9;
    private static final String ITEM_FIXTURE_1_DESCRIPTION = "Simple Laptop";
    private static final String ITEM_FIXTURE_2_ID = "2";
    private static final String ITEM_FIXTURE_2_NAME = "Gaming Laptop";
    private static final int ITEM_FIXTURE_2_QUALITY = 5;
    private static final double ITEM_FIXTURE_2_PRICE = 899.9;
    private static final String ITEM_FIXTURE_2_DESCRIPTION = "High end gaming laptop";
    private MongoClient mongoClient;

    private FrameFixture window;

    @BeforeClass
    public static void startMongo() {
        mongoServer = new MongoServer(new MemoryBackend());
        serverAddress = mongoServer.bind();
    }

    @AfterClass
    public static void stopMongo() {
        if (mongoServer != null) {
            mongoServer.shutdownNow();
        }
    }

    @Override
    protected void onSetUp() throws Exception {

        String containerIpAddress = "localhost";
        Integer mappedPort = serverAddress.getPort();
        mongoClient = new MongoClient(containerIpAddress, mappedPort);
        // always start with an empty database
        mongoClient.getDatabase(DB_NAME).drop();
        // add some students to the database
        addTestItemToDatabase(ITEM_FIXTURE_1_ID, ITEM_FIXTURE_1_NAME,ITEM_FIXTURE_1_QUALITY,ITEM_FIXTURE_1_PRICE, ITEM_FIXTURE_1_DESCRIPTION);
        addTestItemToDatabase(ITEM_FIXTURE_2_ID,ITEM_FIXTURE_2_NAME, ITEM_FIXTURE_2_QUALITY, ITEM_FIXTURE_2_PRICE, ITEM_FIXTURE_2_DESCRIPTION);
        // start the Swing application
        application("com.example.inventorymanager.app.swing.InventorySwingApp")
                .withArgs("--mongo-host=" + containerIpAddress, "--mongo-port=" + mappedPort.toString(),
                        "--db-name=" + DB_NAME, "--db-collection=" + COLLECTION_NAME)
                .start();
        // get a reference of its JFrame
        window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
            @Override
            protected boolean isMatching(JFrame frame) {
                return "Inventory Manager".equals(frame.getTitle()) && frame.isShowing();
            }
        }).using(robot());
    }

    @Override
    protected void onTearDown() {
        mongoClient.close();
    }

    @Test @GUITest
    public void testOnStartAllDatabaseElementsAreShown() {
        waitForInitialItemsLoaded();
        assertThat(window.list().contents())
        .anySatisfy(e -> assertThat(e).contains(ITEM_FIXTURE_1_ID, ITEM_FIXTURE_1_NAME,String.valueOf(ITEM_FIXTURE_1_QUALITY),String.valueOf(ITEM_FIXTURE_1_PRICE), ITEM_FIXTURE_1_DESCRIPTION))
        .anySatisfy(e -> assertThat(e).contains(ITEM_FIXTURE_2_ID,ITEM_FIXTURE_2_NAME, String.valueOf(ITEM_FIXTURE_2_QUALITY), String.valueOf(ITEM_FIXTURE_2_PRICE), ITEM_FIXTURE_2_DESCRIPTION));
    }

    @Test @GUITest
    public void testAddButtonSuccess() {
        waitForInitialItemsLoaded();
        window.textBox("idTextBox").enterText("10");
        window.textBox("nameTextBox").enterText("Mobile");
        window.textBox("quantityTextBox").enterText("15");
        window.textBox("priceTextBox").enterText("599.99");
        window.textBox("descriptionTextBox").enterText("Gaming Phone");
        window.button(JButtonMatcher.withText("Add Item")).click();
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list().contents())
                        .anySatisfy(e -> assertThat(e).contains("10", "Mobile", "15", "599.99", "Gaming Phone"))
        );
    }

    @Test @GUITest
    public void testAddButtonError() {
        waitForInitialItemsLoaded();
        window.textBox("idTextBox").enterText(ITEM_FIXTURE_1_ID);
        window.textBox("nameTextBox").enterText("new one");
        window.textBox("quantityTextBox").enterText("15");
        window.textBox("priceTextBox").enterText("599.99");
        window.textBox("descriptionTextBox").enterText("Gaming Laptop");
        window.button(JButtonMatcher.withText("Add Item")).click();
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.label("errorMessageLabel").text())
                        .contains(ITEM_FIXTURE_1_ID, ITEM_FIXTURE_1_NAME, String.valueOf(ITEM_FIXTURE_1_QUALITY),
                                String.valueOf(ITEM_FIXTURE_1_PRICE), ITEM_FIXTURE_1_DESCRIPTION)
        );
    }

    @Test @GUITest
    public void testDeleteButtonSuccess() {
        waitForInitialItemsLoaded();
        window.list("itemList").selectItem(0);
        window.button(JButtonMatcher.withText("Delete Selected")).click();
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list().contents())
                        .noneMatch(e -> e.contains(ITEM_FIXTURE_1_ID) && e.contains(ITEM_FIXTURE_1_NAME))
        );
    }

    @Test @GUITest
    public void testDeleteButtonError() {
        waitForInitialItemsLoaded();
        window.list("itemList").selectItem(0);
        // ... in the meantime, manually remove the item from the database
        removeTestItemFromDatabase(ITEM_FIXTURE_1_ID);
        // now press the delete button
        window.button(JButtonMatcher.withText("Delete Selected")).click();
        // and verify an error is shown
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.label("errorMessageLabel").text())
                        .contains(ITEM_FIXTURE_1_ID, ITEM_FIXTURE_1_NAME, String.valueOf(ITEM_FIXTURE_1_QUALITY),
                                String.valueOf(ITEM_FIXTURE_1_PRICE), ITEM_FIXTURE_1_DESCRIPTION)
        );
    }

    @Test @GUITest
    public void testUpdateButtonSuccess() {
        waitForInitialItemsLoaded();
        window.list("itemList").selectItem(0);

        window.textBox("nameTextBox").setText("Updated Laptop");
        window.textBox("quantityTextBox").setText("20");
        window.textBox("priceTextBox").setText("1099.99");
        window.textBox("descriptionTextBox").setText("Updated description");

        window.button(JButtonMatcher.withText("Update Selected")).click();

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list().contents())
                        .anySatisfy(e -> assertThat(e).contains("Updated Laptop", "20", "1099.99", "Updated description"))
        );
    }

    private void waitForInitialItemsLoaded() {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.list().contents()).hasSizeGreaterThanOrEqualTo(2)
        );
    }

    private void addTestItemToDatabase(String id, String name, int quantity, double price, String description) {
        mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME).insertOne(new Document()

                .append("id", id).append("name", name).append("quantity", quantity).append("price", price)
                .append("description", description));
    }

    private void removeTestItemFromDatabase(String id) {
        mongoClient
            .getDatabase(DB_NAME)
            .getCollection(COLLECTION_NAME)
            .deleteOne(Filters.eq("id", id));
    }
}
