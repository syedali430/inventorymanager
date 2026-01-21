package com.example.inventorymanager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;

public class ItemRepositoryTestcontainersIT {

    @ClassRule
    public static final MongoDBContainer mongo = new MongoDBContainer("mongo:4.4.3");

    private ItemRepository repo;

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
