package com.example.inventorymanager.repository;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.*;

import java.util.List;

import static org.junit.Assert.*;

public class ItemRepositoryTest {

    private static MongoServer mongoServer;
    private static int port;

    @BeforeClass
    public static void startMongo() {
        mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        port = mongoServer.getLocalAddress().getPort();
    }

    @AfterClass
    public static void stopMongo() {
        if (mongoServer != null) {
            mongoServer.shutdownNow();
        }
    }

    @Before
    public void cleanDb() {
        MongoClient client = new MongoClient("localhost", port);
        client.getDatabase("inventorydb").drop();
        client.close();
    }

    private ItemRepository newRepo() {
        return ItemRepository.create("localhost", port);
    }

    @Test
    public void testAddUpdateDelete() {
        ItemRepository repo = newRepo();
        Item item = new Item("1", "testItem", 5, 10.0, "Desc");
        repo.save(item);

        List<Item> items = repo.findAll();
        assertEquals(1, items.size());
        assertEquals("testItem", items.get(0).getName());

        item.setName("UpdatedItem");
        repo.update(item);
        assertEquals("UpdatedItem", repo.findById("1").get().getName());

        repo.delete("1");
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    public void testDefaultConstructorUsesConfigurablePort() {
        MongoServer localServer = new MongoServer(new MemoryBackend());
        localServer.bind("localhost", 0);
        int customPort = localServer.getLocalAddress().getPort();
        System.setProperty("inventory.mongo.host", "localhost");
        System.setProperty("inventory.mongo.port", String.valueOf(customPort));
        try {
            ItemRepository repo = ItemRepository.createDefault();
            repo.save(new Item("def", "default", 1, 1.0, "d"));
            assertEquals(1, repo.findAll().size());
        } finally {
            System.clearProperty("inventory.mongo.host");
            System.clearProperty("inventory.mongo.port");
            localServer.shutdownNow();
        }
    }

    @Test
    public void testFindAllEmpty() {
        ItemRepository repo = newRepo();
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    public void testFindByIdPresent() {
        ItemRepository repo = newRepo();
        repo.save(new Item("a", "alpha", 1, 2.5, "d"));
        assertTrue(repo.findById("a").isPresent());
        assertEquals("alpha", repo.findById("a").get().getName());
    }

    @Test
    public void testFindByIdMissing() {
        ItemRepository repo = newRepo();
        assertFalse(repo.findById("missing").isPresent());
    }

    @Test
    public void testUpdateChangesFields() {
        ItemRepository repo = newRepo();
        Item item = new Item("u", "old", 1, 1.0, "d");
        repo.save(item);
        item.setQuantity(9);
        item.setPrice(4.5);
        item.setDescription("updated");
        repo.update(item);
        Item loaded = repo.findById("u").get();
        assertEquals(9, loaded.getQuantity());
        assertEquals(4.5, loaded.getPrice(), 0.0);
        assertEquals("updated", loaded.getDescription());
    }

    @Test
    public void testDeleteRemovesOnlyTarget() {
        ItemRepository repo = newRepo();
        repo.save(new Item("1", "one", 1, 1.0, ""));
        repo.save(new Item("2", "two", 2, 2.0, ""));
        repo.delete("1");
        List<Item> items = repo.findAll();
        assertEquals(1, items.size());
        assertEquals("2", items.get(0).getId());
    }

    @Test
    public void testSaveMultipleItems() {
        ItemRepository repo = newRepo();
        repo.save(new Item("1", "one", 1, 1.0, ""));
        repo.save(new Item("2", "two", 2, 2.0, ""));
        repo.save(new Item("3", "three", 3, 3.0, ""));
        assertEquals(3, repo.findAll().size());
    }

    @Test
    public void testUpdateNonExistingDoesNotThrow() {
        ItemRepository repo = newRepo();
        Item ghost = new Item("ghost", "none", 0, 0.0, "");
        repo.update(ghost);
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    public void testDefaultPortFallsBackOnInvalidValue() throws Exception {
        System.setProperty("inventory.mongo.port", "not-a-number");
        try {
            java.lang.reflect.Method m = ItemRepository.class.getDeclaredMethod("defaultPort");
            m.setAccessible(true);
            int fallbackPort = (int) m.invoke(null);
            assertEquals(27017, fallbackPort);
        } finally {
            System.clearProperty("inventory.mongo.port");
        }
    }

    @Test
    public void testDefaultHostUsesSystemProperty() throws Exception {
        System.setProperty("inventory.mongo.host", "customhost");
        try {
            java.lang.reflect.Method m = ItemRepository.class.getDeclaredMethod("defaultHost");
            m.setAccessible(true);
            String host = (String) m.invoke(null);
            assertEquals("customhost", host);
        } finally {
            System.clearProperty("inventory.mongo.host");
        }
    }

    @Test
    public void testDefaultHostFallsBackToLocalhost() throws Exception {
        System.clearProperty("inventory.mongo.host");
        java.lang.reflect.Method m = ItemRepository.class.getDeclaredMethod("defaultHost");
        m.setAccessible(true);
        String host = (String) m.invoke(null);
        assertEquals("localhost", host);
    }

    @Test
    public void testDefaultPortUsesValidSystemProperty() throws Exception {
        System.setProperty("inventory.mongo.port", "27018");
        try {
            java.lang.reflect.Method m = ItemRepository.class.getDeclaredMethod("defaultPort");
            m.setAccessible(true);
            int portValue = (int) m.invoke(null);
            assertEquals(27018, portValue);
        } finally {
            System.clearProperty("inventory.mongo.port");
        }
    }

    @Test
    public void testDefaultDatabaseAndCollectionNames() throws Exception {
        System.clearProperty("inventory.mongo.db");
        System.clearProperty("inventory.mongo.collection");
        java.lang.reflect.Method dbMethod = ItemRepository.class.getDeclaredMethod("defaultDatabaseName");
        java.lang.reflect.Method collectionMethod = ItemRepository.class.getDeclaredMethod("defaultCollectionName");
        dbMethod.setAccessible(true);
        collectionMethod.setAccessible(true);
        assertEquals("inventorydb", dbMethod.invoke(null));
        assertEquals("items", collectionMethod.invoke(null));
    }

    @Test
    public void testDefaultDatabaseAndCollectionNamesUseOverrides() throws Exception {
        System.setProperty("inventory.mongo.db", "customdb");
        System.setProperty("inventory.mongo.collection", "customitems");
        try {
            java.lang.reflect.Method dbMethod = ItemRepository.class.getDeclaredMethod("defaultDatabaseName");
            java.lang.reflect.Method collectionMethod = ItemRepository.class.getDeclaredMethod("defaultCollectionName");
            dbMethod.setAccessible(true);
            collectionMethod.setAccessible(true);
            assertEquals("customdb", dbMethod.invoke(null));
            assertEquals("customitems", collectionMethod.invoke(null));
        } finally {
            System.clearProperty("inventory.mongo.db");
            System.clearProperty("inventory.mongo.collection");
        }
    }

    @Test
    public void testCreateWithCustomDatabaseAndCollection() {
        ItemRepository repo = ItemRepository.create("localhost", port, "customdb", "customitems");
        repo.save(new Item("c1", "custom", 2, 2.5, "d"));
        assertEquals(1, repo.findAll().size());
        assertEquals("custom", repo.findById("c1").get().getName());
    }
}
