package com.example.inventorymanager.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import java.net.InetSocketAddress;

public class ItemMongoRepositoryTestcontainersIT {

    private static MongoServer mongoServer;
    private static InetSocketAddress serverAddress;

    private MongoClient client;
    private ItemMongoRepository itemRepository;
    private MongoCollection<Document> itemCollection;

    public static final String ITEM_COLLECTION_NAME = "item";
    public static final String INVENTORY_DB_NAME = "inventory";

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
    public void setup() {
        client = new MongoClient(new ServerAddress(serverAddress));
        itemRepository = new ItemMongoRepository(client, INVENTORY_DB_NAME, ITEM_COLLECTION_NAME);
        MongoDatabase database = client.getDatabase(INVENTORY_DB_NAME);
        database.drop();
        itemCollection = database.getCollection(ITEM_COLLECTION_NAME);
    }

    @After
    public void tearDown() {
        client.close();
    }

    @Test
    public void testFindAll() {
        addTestItemToDatabase("1", "Laptop", 10, 999.99, "High-end gaming laptop");
        addTestItemToDatabase("2", "Laptop", 1, 99.99, "gaming laptop");
        assertThat(itemRepository.findAll())
                .containsExactly(
                        new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop"),
                        new Item("2", "Laptop", 1, 99.99, "gaming laptop"));
    }

    @Test
    public void testFindById() {
        addTestItemToDatabase("1", "Laptop", 10, 999.99, "High-end gaming laptop");
        addTestItemToDatabase("2", "Laptop", 1, 99.99, "gaming laptop");
        assertThat(itemRepository.findById("2"))
                .contains(new Item("2", "Laptop", 1, 99.99, "gaming laptop"));
    }

    @Test
    public void testSave() {
        Item item = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
        itemRepository.save(item);
        assertThat(readAllItemFromDatabase())
                .containsExactly(item);
    }

    @Test
    public void testDelete() {
        addTestItemToDatabase("1", "Laptop", 10, 999.99, "High-end gaming laptop");
        itemRepository.delete("1");
        assertThat(readAllItemFromDatabase()).isEmpty();
    }

    @Test
    public void testUpdate() {
        Item originalItem = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
        itemRepository.save(originalItem);

        assertThat(readAllItemFromDatabase()).containsExactly(originalItem);

        Item updatedItem = new Item("1", "Laptop", 15, 899.99, "Updated laptop");
        itemRepository.update(updatedItem);
        assertThat(readAllItemFromDatabase()).containsExactly(updatedItem);
    }

    private void addTestItemToDatabase(String id, String name, int quantity, double price, String description) {
        itemCollection.insertOne(new Document()
                .append("id", id)
                .append("name", name)
                .append("quantity", quantity)
                .append("price", price)
                .append("description", description));
    }

    private List<Item> readAllItemFromDatabase() {
        return StreamSupport.stream(itemCollection.find().spliterator(), false)
                .map(d -> new Item(
                        String.valueOf(d.get("id")),
                        String.valueOf(d.get("name")),
                        ((Number) d.get("quantity")).intValue(),
                        ((Number) d.get("price")).doubleValue(),
                        String.valueOf(d.get("description"))))
                .collect(Collectors.toList());
    }
}
