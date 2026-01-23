package com.example.inventorymanager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;

public class ItemRepositoryTestcontainersIT {

    private static String mongoHost;
    private static int mongoPort;

    private ItemRepository repo;

    @BeforeClass
    public static void startMongo() {
        String portValue = System.getProperty("mongo.port");
        Assume.assumeTrue("Docker not available", portValue != null && portValue.matches("\\d+"));
        mongoHost = System.getProperty("mongo.host", "localhost");
        mongoPort = Integer.parseInt(portValue);
    }

    @Before
    public void setUp() {
        try (MongoClient client = new MongoClient(mongoHost, mongoPort)) {
            client.getDatabase("inventorydb").drop();
        }
        repo = ItemRepository.create(mongoHost, mongoPort, "inventorydb", "items");
    }

    @Test
    public void testSaveAndFind() {
        repo.save(new Item("tc-1", "Keyboard", 2, 49.99, "Mechanical"));
        List<Item> items = repo.findAll();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("Keyboard");
    }

    @Test
    public void testFindByIdMissing() {
        assertThat(repo.findById("missing")).isNotPresent();
    }
}
