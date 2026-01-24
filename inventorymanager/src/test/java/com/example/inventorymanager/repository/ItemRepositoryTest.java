package com.example.inventorymanager.repository;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ItemRepositoryTest {

    @Test
    public void testSaveFindUpdateDeleteFlow() {
        MongoServer server = new MongoServer(new MemoryBackend());
        server.bind("localhost", 0);
        int port = server.getLocalAddress().getPort();
        MongoClient client = new MongoClient("localhost", port);

        try {
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
            assertEquals(Double.valueOf(109.0), updatedFound.get().getPrice());
            assertEquals("wider chair", updatedFound.get().getDescription());

            repo.delete("2");
            assertFalse(repo.findById("2").isPresent());
            assertEquals(1, repo.findAll().size());
        } finally {
            client.close();
            server.shutdownNow();
        }
    }

    @Test
    public void testFindByIdReturnsEmptyWhenMissing() {
        MongoServer server = new MongoServer(new MemoryBackend());
        server.bind("localhost", 0);
        int port = server.getLocalAddress().getPort();
        MongoClient client = new MongoClient("localhost", port);

        try {
            ItemRepository repo = ItemRepository.create(client);
            assertFalse(repo.findById("missing").isPresent());
        } finally {
            client.close();
            server.shutdownNow();
        }
    }
}
