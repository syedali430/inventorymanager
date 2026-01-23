package com.example.inventorymanager.repository;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemRepositoryIT {

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
        try (MongoClient client = new MongoClient("localhost", port)) {
            client.getDatabase("inventorydb").drop();
        }
    }

    private ItemRepository newRepo() {
        return ItemRepository.create("localhost", port, "inventorydb", "items");
    }

    @Test
    public void testSavePersistsItem() {
        ItemRepository repo = newRepo();
        Item item = new Item("1", "testItem", 5, 10.0, "Desc");
        repo.save(item);

        List<Item> items = repo.findAll();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("testItem");
    }

    @Test
    public void testUpdatePersistsChanges() {
        ItemRepository repo = newRepo();
        Item item = new Item("1", "testItem", 5, 10.0, "Desc");
        repo.save(item);

        item.setName("UpdatedItem");
        repo.update(item);
        assertThat(repo.findById("1")).isPresent();
        assertThat(repo.findById("1").get().getName()).isEqualTo("UpdatedItem");
    }

    @Test
    public void testDeleteRemovesItem() {
        ItemRepository repo = newRepo();
        repo.save(new Item("1", "testItem", 5, 10.0, "Desc"));

        repo.delete("1");
        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    public void testDefaultConstructorUsesConfigurablePort() {
        MongoServer localServer = new MongoServer(new MemoryBackend());
        localServer.bind("localhost", 0);
        int customPort = localServer.getLocalAddress().getPort();
        System.setProperty("mongo.host", "localhost");
        System.setProperty("mongo.port", String.valueOf(customPort));
        try {
            ItemRepository repo = ItemRepository.createDefault();
            repo.save(new Item("def", "default", 1, 1.0, "d"));
            assertThat(repo.findAll()).hasSize(1);
        } finally {
            System.clearProperty("mongo.host");
            System.clearProperty("mongo.port");
            localServer.shutdownNow();
        }
    }

    @Test
    public void testCreateWithMongoClientUsesDefaultDatabaseAndCollection() {
        try (MongoClient client = new MongoClient("localhost", port)) {
            ItemRepository repo = ItemRepository.create(client);
            repo.save(new Item("ctor", "default", 1, 1.0, "d"));
            assertThat(repo.findAll()).hasSize(1);
        }
    }

    @Test
    public void testCreateUsesDefaultDatabaseAndCollection() {
        ItemRepository repo = ItemRepository.create("localhost", port);
        repo.save(new Item("factory", "default", 1, 1.0, "d"));
        assertThat(repo.findAll()).hasSize(1);
    }

    @Test
    public void testFindAllEmpty() {
        ItemRepository repo = newRepo();
        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    public void testFindByIdPresent() {
        ItemRepository repo = newRepo();
        repo.save(new Item("a", "alpha", 1, 2.5, "d"));
        assertThat(repo.findById("a")).isPresent();
        assertThat(repo.findById("a").get().getName()).isEqualTo("alpha");
    }

    @Test
    public void testFindByIdMissing() {
        ItemRepository repo = newRepo();
        assertThat(repo.findById("missing")).isNotPresent();
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
        assertThat(loaded.getQuantity()).isEqualTo(9);
        assertThat(loaded.getPrice()).isEqualTo(4.5);
        assertThat(loaded.getDescription()).isEqualTo("updated");
    }

    @Test
    public void testDeleteRemovesOnlyTarget() {
        ItemRepository repo = newRepo();
        repo.save(new Item("1", "one", 1, 1.0, ""));
        repo.save(new Item("2", "two", 2, 2.0, ""));
        repo.delete("1");
        List<Item> items = repo.findAll();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo("2");
    }

    @Test
    public void testSaveMultipleItems() {
        ItemRepository repo = newRepo();
        repo.save(new Item("1", "one", 1, 1.0, ""));
        repo.save(new Item("2", "two", 2, 2.0, ""));
        repo.save(new Item("3", "three", 3, 3.0, ""));
        assertThat(repo.findAll()).hasSize(3);
    }

    @Test
    public void testUpdateNonExistingDoesNotThrow() {
        ItemRepository repo = newRepo();
        Item ghost = new Item("ghost", "none", 0, 0.0, "");
        repo.update(ghost);
        assertThat(repo.findAll()).isEmpty();
    }
}
