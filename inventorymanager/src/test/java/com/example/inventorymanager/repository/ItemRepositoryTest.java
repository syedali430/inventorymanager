package com.example.inventorymanager.repository;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class ItemRepositoryTest {

    private static MongodExecutable mongodExecutable;
    private static MongodProcess mongod;
    private static int port;

    @BeforeClass
    public static void startMongo() throws Exception {
        port = Network.getFreeServerPort();
        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.V4_4)
                .net(new Net(port, Network.localhostIsIPv6()))
                .build();
        MongodStarter starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(mongodConfig);
        mongod = mongodExecutable.start();
    }

    @AfterClass
    public static void stopMongo() {
        if (mongod != null) {
            mongod.stop();
        }
        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }

    @Before
    public void cleanDb() {
        MongoClient client = new MongoClient("localhost", port);
        client.getDatabase("inventorydb").drop();
        client.close();
    }

    private ItemRepository newRepo() {
        return new ItemRepository("localhost", port);
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
}

