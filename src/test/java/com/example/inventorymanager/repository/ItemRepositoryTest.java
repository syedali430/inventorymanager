package com.example.inventorymanager.repository;

import com.example.inventorymanager.model.Item;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
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

    @Test
    public void testAddUpdateDelete() {
        ItemRepository repo = new ItemRepository("localhost", port);
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
}
