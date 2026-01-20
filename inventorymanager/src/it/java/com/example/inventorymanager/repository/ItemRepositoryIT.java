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
        MongoClient client = new MongoClient("localhost", port);
        client.getDatabase("inventorydb").drop();
        client.close();
    }

    private ItemRepository newRepo() {
        return ItemRepository.create("localhost", port, "inventorydb", "items");
    }

    @Test
    public void testSaveAndFindUsingEmbeddedMongo() {
        ItemRepository repo = newRepo();
        Item item = new Item("it-1", "ContainerItem", 9, 19.5, "desc");
        repo.save(item);

        List<Item> items = repo.findAll();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("ContainerItem");
    }

    @Test
    public void testFindByIdEmptyWhenMissing() {
        ItemRepository repo = newRepo();
        assertThat(repo.findById("missing")).isNotPresent();
    }
}
