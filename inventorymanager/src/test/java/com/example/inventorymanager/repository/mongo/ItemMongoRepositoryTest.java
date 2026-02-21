package com.example.inventorymanager.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public class ItemMongoRepositoryTest {

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
        client.getDatabase("inventory").drop();
        client.close();
    }

    private ItemMongoRepository newRepo() {
        return new ItemMongoRepository(new MongoClient("localhost", port), "inventory", "item");
    }

    @Test
    public void testSaveFindUpdateDeleteFlow() {
        ItemMongoRepository repo = newRepo();
        Item first = new Item("1", "Chair", 3, 99.5, "desk chair");
        Item second = new Item("2", "Lamp", 1, 19.0, "bedside lamp");

        repo.save(first);
        repo.save(second);

        List<Item> all = repo.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(Item::getName).containsExactly("Chair", "Lamp");

        Item updated = new Item("2", "Lamp", 2, 22.0, "updated");
        repo.update(updated);
        assertThat(repo.findById("2")).contains(updated);

        repo.delete("1");
        assertThat(repo.findAll()).hasSize(1);
        assertThat(repo.findById("1")).isNotPresent();
    }

    @Test
    public void testFindByIdMissing() {
        ItemMongoRepository repo = newRepo();
        assertThat(repo.findById("missing")).isNotPresent();
    }

    @Test
    public void testFindAllEmpty() {
        ItemMongoRepository repo = newRepo();
        assertThat(repo.findAll()).isEmpty();
    }
}
