package com.example.inventorymanager.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.ItemRepositoryInterface;
import com.example.inventorymanager.repository.mongo.ItemMongoRepository;
import com.example.inventorymanager.view.InventoryView;
import com.mongodb.MongoClient;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import java.net.InetSocketAddress;

public class ItemControllerIT {

    @Mock
    private InventoryView inventoryView;

    private ItemRepositoryInterface itemRepository;

    private ItemController itemController;

    private AutoCloseable closeable;

    public static final String ITEM_COLLECTION_NAME = "item";
    public static final String INVENTORY_DB_NAME = "inventory";

    private static MongoServer mongoServer;
    private static InetSocketAddress serverAddress;

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

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        itemRepository = new ItemMongoRepository(new MongoClient("localhost", serverAddress.getPort()), INVENTORY_DB_NAME,
                ITEM_COLLECTION_NAME);
        // explicit empty the database through the repository
        for (Item item : itemRepository.findAll()) {
            itemRepository.delete(item.getId());
        }
        itemController = new ItemController(itemRepository, inventoryView);
    }

    @After
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    public void testAllItems() {
        Item item = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
        itemRepository.save(item);
        itemController.getAllItems();
        verify(inventoryView)
            .displayItems(asList(item));
    }

    @Test
    public void testNewItem() {
        Item item = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
        itemController.addItem(item);
        verify(inventoryView).addItem(item);
    }

    @Test
    public void testDeleteItem() {
        Item itemToDelete = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
        itemRepository.save(itemToDelete);
        itemController.deleteItem(itemToDelete);
        verify(inventoryView).deleteItem(itemToDelete);
    }

    @Test
    public void testUpdateItem() {
        Item originalItem = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
        itemRepository.save(originalItem);

        Item updatedItem = new Item("1", "Laptop", 15, 899.99, "Updated laptop");
        itemController.updateItem(updatedItem);

        verify(inventoryView).updateItem(updatedItem);
    }

}
