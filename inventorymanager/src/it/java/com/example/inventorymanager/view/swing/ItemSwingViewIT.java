package com.example.inventorymanager.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.inventorymanager.controller.ItemController;
import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.mongo.ItemMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import org.awaitility.Awaitility;


@RunWith(GUITestRunner.class)
public class ItemSwingViewIT extends AssertJSwingJUnitTestCase{

    private static MongoServer server;
    private static InetSocketAddress serverAddress;

    private MongoClient mongoClient;

    private FrameFixture window;
    private InventorySwingView inventorySwingView;
    private ItemController itemController;
    private ItemMongoRepository itemRepository;

    public static final String ITEM_COLLECTION_NAME = "item";
    public static final String INVENTORY_DB_NAME = "inventory";

    @BeforeClass
    public static void setupServer() {
        server = new MongoServer(new MemoryBackend());
        // bind on a random local port
        serverAddress = server.bind();
    }

    @AfterClass
    public static void shutdownServer() {
        server.shutdown();
    }

    @Override
    protected void onSetUp() {
        mongoClient = new MongoClient(new ServerAddress(serverAddress));
        itemRepository = new ItemMongoRepository(mongoClient,INVENTORY_DB_NAME,ITEM_COLLECTION_NAME);
        // explicit empty the database through the repository
        for (Item item : itemRepository.findAll()) {
            itemRepository.delete(item.getId());
        }
        GuiActionRunner.execute(() -> {
            inventorySwingView = new InventorySwingView();
            itemController = new ItemController(itemRepository, inventorySwingView);
            inventorySwingView.setItemController(itemController);
            return inventorySwingView;
        });
        window = new FrameFixture(robot(), inventorySwingView);
        window.show(); // shows the frame to test
    }

    @Override
    protected void onTearDown() {
        mongoClient.close();
    }

    @Test @GUITest
    public void testAllItem() {
        // use the repository to add Items to the database
        Item item1 = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
        Item item2 = new Item("2", "Laptop", 12, 599.99, "gaming laptop");
        itemRepository.save(item1);
        itemRepository.save(item2);
        // use the controller's allItems
        GuiActionRunner.execute(
            () -> itemController.getAllItems());
        // and verify that the view's list is populated
        assertThat(window.list().contents())
            .containsExactly(displayString(item1), displayString(item2));
    }

    @Test @GUITest
    public void testAddButtonSuccess() {
        window.textBox("idTextBox").enterText("1");
        window.textBox("nameTextBox").enterText("Laptop");
        window.textBox("quantityTextBox").enterText("10");
        window.textBox("priceTextBox").enterText("999.99");
        window.textBox("descriptionTextBox").enterText("Gaming Laptop");
        window.button(JButtonMatcher.withText("Add Item")).click();

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(window.list().contents())
                .containsExactly(displayString(new Item("1", "Laptop", 10, 999.99, "Gaming Laptop")))
        );

    }

    @Test @GUITest
    public void testDeleteButtonSuccess() {
        // use the controller to populate the view's list...
        itemController.addItem(new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop"));

        // ...with an item to select
        window.list().selectItem(0);
        window.button(JButtonMatcher.withText("Delete Selected")).click();

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(window.list().contents())
                .isEmpty()
        );
    }

    @Test @GUITest
    public void testUpdateButtonSuccess() {
        // Step 1: Add an item to the repository so we have something to update
        Item item = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
        itemRepository.save(item);

        GuiActionRunner.execute(() -> {
            inventorySwingView.getListItemModel().addElement(item);
        });

        window.list().selectItem(0);

        window.textBox("idTextBox").requireText("1");
        window.textBox("idTextBox").requireDisabled();

        window.textBox("nameTextBox").setText("Updated Laptop");
        window.textBox("quantityTextBox").setText("15");
        window.textBox("priceTextBox").setText("799.99");
        window.textBox("descriptionTextBox").setText("Updated Gaming Laptop");

        window.button(JButtonMatcher.withText("Update Selected")).click();

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(window.list().contents())
                .containsExactly(displayString(new Item("1", "Updated Laptop", 15, 799.99, "Updated Gaming Laptop")))
        );
    }

    private static String displayString(Item item) {
        return item.getId() + " - " + item.getName() + " - " + item.getQuantity() + " - "
                + item.getPrice() + " - " + item.getDescription();
    }

}
