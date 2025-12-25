package com.example.inventorymanager.repository;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemRepositoryIT {

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


