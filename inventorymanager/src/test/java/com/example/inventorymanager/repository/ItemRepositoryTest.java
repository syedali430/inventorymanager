package com.example.inventorymanager.repository;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ItemRepositoryTest {

    @Test
    public void testSaveFindUpdateDeleteFlow() {
        MongoServer server = new MongoServer(new MemoryBackend());
        server.bind("localhost", 0);
        int port = server.getLocalAddress().getPort();
        try (MongoClient client = new MongoClient("localhost", port)) {
            ItemRepository repo = ItemRepository.create(client);

            Item first = new Item("1", "Chair", 3, 99.5, "desk chair");
            Item second = new Item("2", "Lamp", 1, 19.0, "bedside lamp");
            repo.save(first);
            repo.save(second);

            List<Item> all = repo.findAll();
            assertEquals(2, all.size());
            assertTrue(all.stream().anyMatch(item -> "1".equals(item.getId())));
            assertTrue(all.stream().anyMatch(item -> "2".equals(item.getId())));

            Optional<Item> found = repo.findById("1");
            assertTrue(found.isPresent());
            assertEquals("Chair", found.get().getName());

            Item updated = new Item("1", "Chair XL", 5, 109.0, "wider chair");
            repo.update(updated);
            Optional<Item> updatedFound = repo.findById("1");
            assertTrue(updatedFound.isPresent());
            assertEquals("Chair XL", updatedFound.get().getName());
            assertEquals(5, updatedFound.get().getQuantity());
            assertEquals(109.0, updatedFound.get().getPrice(), 0.0);
            assertEquals("wider chair", updatedFound.get().getDescription());

            repo.delete("2");
            assertFalse(repo.findById("2").isPresent());
            assertEquals(1, repo.findAll().size());
        } finally {
            server.shutdownNow();
        }
    }

    @Test
    public void testFindByIdReturnsEmptyWhenMissing() {
        MongoServer server = new MongoServer(new MemoryBackend());
        server.bind("localhost", 0);
        int port = server.getLocalAddress().getPort();
        try (MongoClient client = new MongoClient("localhost", port)) {
            ItemRepository repo = ItemRepository.create(client);
            assertFalse(repo.findById("missing").isPresent());
        } finally {
            server.shutdownNow();
        }
    }

    @Test
    public void testFindAllReturnsEmptyWhenNoItems() {
        MongoServer server = new MongoServer(new MemoryBackend());
        server.bind("localhost", 0);
        int port = server.getLocalAddress().getPort();
        try (MongoClient client = new MongoClient("localhost", port)) {
            ItemRepository repo = ItemRepository.create(client);
            List<Item> items = repo.findAll();
            assertEquals(0, items.size());
        } finally {
            server.shutdownNow();
        }
    }

    @Test
    public void testDeleteMissingItemDoesNotFail() {
        MongoServer server = new MongoServer(new MemoryBackend());
        server.bind("localhost", 0);
        int port = server.getLocalAddress().getPort();
        try (MongoClient client = new MongoClient("localhost", port)) {
            ItemRepository repo = ItemRepository.create(client);
            repo.delete("missing");
            assertEquals(0, repo.findAll().size());
        } finally {
            server.shutdownNow();
        }
    }

    @Test
    public void testCreateDefaultUsesInventoryProperties() {
        MongoServer server = new MongoServer(new MemoryBackend());
        server.bind("localhost", 0);
        int port = server.getLocalAddress().getPort();

        System.setProperty("inventory.mongo.host", "localhost");
        System.setProperty("inventory.mongo.port", String.valueOf(port));
        System.setProperty("inventory.mongo.db", "testdb");
        System.setProperty("inventory.mongo.collection", "testitems");

        try {
            ItemRepository repo = ItemRepository.createDefault();
            repo.save(new Item("10", "Desk", 2, 150.0, "work desk"));
            assertTrue(repo.findById("10").isPresent());
        } finally {
            System.clearProperty("inventory.mongo.host");
            System.clearProperty("inventory.mongo.port");
            System.clearProperty("inventory.mongo.db");
            System.clearProperty("inventory.mongo.collection");
            server.shutdownNow();
        }
    }

    @Test
    public void testCreateDefaultWithoutPropertiesCreatesRepository() throws Exception {
        System.clearProperty("inventory.mongo.host");
        System.clearProperty("inventory.mongo.port");
        System.clearProperty("inventory.mongo.db");
        System.clearProperty("inventory.mongo.collection");

        ItemRepository repo = ItemRepository.createDefault();
        assertNotNull(repo);

        java.lang.reflect.Field clientField = ItemRepository.class.getDeclaredField("client");
        clientField.setAccessible(true);
        MongoClient client = (MongoClient) clientField.get(repo);
        client.close();
    }

    @Test
    public void testCreateWithHostPortUsesDefaultNames() {
        MongoServer server = new MongoServer(new MemoryBackend());
        server.bind("localhost", 0);
        int port = server.getLocalAddress().getPort();

        System.setProperty("inventory.mongo.db", "hostportdb");
        System.setProperty("inventory.mongo.collection", "hostportitems");

        try {
            ItemRepository repo = ItemRepository.create("localhost", port);
            repo.save(new Item("20", "Lamp", 1, 35.0, "desk lamp"));
            assertEquals(1, repo.findAll().size());
        } finally {
            System.clearProperty("inventory.mongo.db");
            System.clearProperty("inventory.mongo.collection");
            server.shutdownNow();
        }
    }

    @Test
    public void testCreateWithHostPortAndNames() {
        MongoServer server = new MongoServer(new MemoryBackend());
        server.bind("localhost", 0);
        int port = server.getLocalAddress().getPort();

        try {
            ItemRepository repo = ItemRepository.create("localhost", port, "customdb", "customitems");
            repo.save(new Item("30", "Shelf", 4, 79.0, "wood shelf"));
            assertTrue(repo.findById("30").isPresent());
        } finally {
            server.shutdownNow();
        }
    }

    @Test
    public void testDefaultHostPrefersMongoHostProperty() {
        System.setProperty("mongo.host", "primary-host");
        System.setProperty("inventory.mongo.host", "fallback-host");
        try {
            assertEquals("primary-host", ItemRepository.defaultHost());
        } finally {
            System.clearProperty("mongo.host");
            System.clearProperty("inventory.mongo.host");
        }
    }

    @Test
    public void testDefaultHostUsesInventoryHostWhenPrimaryMissing() {
        System.clearProperty("mongo.host");
        System.setProperty("inventory.mongo.host", "inventory-host");
        try {
            assertEquals("inventory-host", ItemRepository.defaultHost());
        } finally {
            System.clearProperty("inventory.mongo.host");
        }
    }

    @Test
    public void testDefaultHostFallsBackToLocalhost() {
        System.clearProperty("mongo.host");
        System.clearProperty("inventory.mongo.host");
        assertEquals("localhost", ItemRepository.defaultHost());
    }

    @Test
    public void testDefaultPortPrefersMongoPortProperty() {
        System.setProperty("mongo.port", "12345");
        System.setProperty("inventory.mongo.port", "27018");
        try {
            assertEquals(12345, ItemRepository.defaultPort());
        } finally {
            System.clearProperty("mongo.port");
            System.clearProperty("inventory.mongo.port");
        }
    }

    @Test
    public void testDefaultPortUsesInventoryPortWhenPrimaryMissing() {
        System.clearProperty("mongo.port");
        System.setProperty("inventory.mongo.port", "27018");
        try {
            assertEquals(27018, ItemRepository.defaultPort());
        } finally {
            System.clearProperty("inventory.mongo.port");
        }
    }

    @Test
    public void testDefaultPortFallsBackWhenInvalid() {
        System.setProperty("mongo.port", "not-a-number");
        try {
            assertEquals(27017, ItemRepository.defaultPort());
        } finally {
            System.clearProperty("mongo.port");
        }
    }

    @Test
    public void testDefaultPortFallsBackWhenInventoryPortInvalid() {
        System.clearProperty("mongo.port");
        System.setProperty("inventory.mongo.port", "bad-port");
        try {
            assertEquals(27017, ItemRepository.defaultPort());
        } finally {
            System.clearProperty("inventory.mongo.port");
        }
    }

    @Test
    public void testDefaultDatabaseAndCollectionNames() throws Exception {
        System.clearProperty("inventory.mongo.db");
        System.clearProperty("inventory.mongo.collection");
        Method dbMethod = ItemRepository.class.getDeclaredMethod("defaultDatabaseName");
        Method collectionMethod = ItemRepository.class.getDeclaredMethod("defaultCollectionName");
        dbMethod.setAccessible(true);
        collectionMethod.setAccessible(true);
        String dbName = (String) dbMethod.invoke(null);
        String collectionName = (String) collectionMethod.invoke(null);
        assertEquals("inventorydb", dbName);
        assertEquals("items", collectionName);
    }

    @Test
    public void testDefaultDatabaseAndCollectionNamesFromProperties() throws Exception {
        System.setProperty("inventory.mongo.db", "customdb");
        System.setProperty("inventory.mongo.collection", "customcollection");
        Method dbMethod = ItemRepository.class.getDeclaredMethod("defaultDatabaseName");
        Method collectionMethod = ItemRepository.class.getDeclaredMethod("defaultCollectionName");
        dbMethod.setAccessible(true);
        collectionMethod.setAccessible(true);
        try {
            String dbName = (String) dbMethod.invoke(null);
            String collectionName = (String) collectionMethod.invoke(null);
            assertEquals("customdb", dbName);
            assertEquals("customcollection", collectionName);
        } finally {
            System.clearProperty("inventory.mongo.db");
            System.clearProperty("inventory.mongo.collection");
        }
    }
}
