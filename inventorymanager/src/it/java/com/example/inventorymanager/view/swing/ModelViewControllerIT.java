package com.example.inventorymanager.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.awaitility.Awaitility;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MongoDBContainer;

import com.example.inventorymanager.repository.mongo.ItemMongoRepository;
import com.example.inventorymanager.controller.ItemController;
import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

@RunWith(GUITestRunner.class)
public class ModelViewControllerIT extends AssertJSwingJUnitTestCase{

    @ClassRule
    public static final MongoDBContainer mongo =
        new MongoDBContainer("mongo:4.4.3");

    private MongoClient mongoClient;

    private FrameFixture window;
    private ItemMongoRepository itemRepository;
    private ItemController itemController;

    public static final String ITEM_COLLECTION_NAME = "item";
    public static final String INVENTORY_DB_NAME = "inventory";


    @Override
    protected void onSetUp() {

        mongoClient = new MongoClient(
                new ServerAddress(
                    mongo.getHost(),
                    mongo.getFirstMappedPort()));
            itemRepository = new ItemMongoRepository(mongoClient,INVENTORY_DB_NAME,ITEM_COLLECTION_NAME);
            // explicit empty the database through the repository
            for (Item item : itemRepository.findAll()) {
                itemRepository.delete(item.getId());
            }
            window = new FrameFixture(robot(), GuiActionRunner.execute(() -> {
                InventorySwingView itemSwingView = new InventorySwingView();
                itemController = new ItemController(itemRepository, itemSwingView);
                itemSwingView.setItemController(itemController);
                return itemSwingView;
            }));
            window.show(); // shows the frame to test
    }

    @Override
    protected void onTearDown() {
        mongoClient.close();
    }

    @Test
    public void testAddItem() {
        // use the UI to add a item...
        window.textBox("idTextBox").enterText("1");
        window.textBox("nameTextBox").enterText("Laptop");
        window.textBox("quantityTextBox").enterText("10");
        window.textBox("priceTextBox").enterText("999.99");
        window.textBox("descriptionTextBox").enterText("Gaming Laptop");
        window.button(JButtonMatcher.withText("Add Item")).click();
        // ...verify that it has been added to the database
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> 
            assertThat(itemRepository.findById("1"))
                .contains(new Item("1", "Laptop", 10, 999.99, "Gaming Laptop"))
        );
    }

    @Test
    public void testDeleteItem() {
        // add a item needed for tests
        itemRepository.save(new Item("1", "Laptop", 10, 999.99, "Gaming Laptop"));
        // use the controller's allItems to make the Item
        // appear in the GUI list
        GuiActionRunner.execute(
            () -> itemController.getAllItems());
        // ...select the existing Item
        window.list().selectItem(0);
        window.button(JButtonMatcher.withText("Delete Selected")).click();
        // verify that the Item has been deleted from the db
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> 
            assertThat(itemRepository.findById("1"))
                .isNotPresent()
        );
    }

    @Test
    public void testUpdateItem() {
        itemRepository.save(new Item("1", "Laptop", 10, 999.99, "Gaming Laptop"));

        GuiActionRunner.execute(() -> itemController.getAllItems());

        window.list().selectItem(0);

        window.textBox("idTextBox").requireText("1");
        window.textBox("idTextBox").requireDisabled();

        window.textBox("nameTextBox").setText("Updated Laptop");
        window.textBox("quantityTextBox").setText("15");
        window.textBox("priceTextBox").setText("899.99");
        window.textBox("descriptionTextBox").setText("Updated Gaming Laptop");

        window.button(JButtonMatcher.withText("Update Selected")).click();

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> 
            assertThat(itemRepository.findById("1"))
                .contains(new Item("1", "Updated Laptop", 15, 899.99, "Updated Gaming Laptop"))
        );

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> 
            assertThat(window.list().contents())
                .containsExactly(new Item("1", "Updated Laptop", 15, 899.99, "Updated Gaming Laptop").toString())
        );
    }



}