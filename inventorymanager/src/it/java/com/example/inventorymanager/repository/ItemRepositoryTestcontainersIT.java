package com.example.inventorymanager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MongoDBContainer;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;

public class ItemRepositoryTestcontainersIT {

    private static MongoDBContainer mongo;

    private ItemRepository repo;

    @BeforeClass
    public static void startMongo() {
        Assume.assumeTrue("Docker not available", DockerClientFactory.instance().isDockerAvailable());
        mongo = new MongoDBContainer("mongo:4.4.3");
        mongo.start();
    }

    @AfterClass
    public static void stopMongo() {
        if (mongo != null) {
            mongo.stop();
        }
    }

    @Before
    public void setUp() {
        try (MongoClient client = new MongoClient(mongo.getHost(), mongo.getFirstMappedPort())) {
            client.getDatabase("inventorydb").drop();
        }
        repo = ItemRepository.create(mongo.getHost(), mongo.getFirstMappedPort(), "inventorydb", "items");
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
