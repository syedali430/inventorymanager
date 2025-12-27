package com.example.inventorymanager.repository;

import com.example.inventorymanager.model.Item;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemRepository {

    private final MongoCollection<Document> collection;
    private final MongoClient client;

    public ItemRepository() {
        this(new MongoClient(defaultHost(), defaultPort()));
    }

    public ItemRepository(String host, int port) {
        this(new MongoClient(host, port));
    }

    public ItemRepository(MongoClient client) {
        this.client = client;
        MongoDatabase database = client.getDatabase("inventorydb");
        collection = database.getCollection("items");
    }

    private static String defaultHost() {
        return System.getProperty("inventory.mongo.host", "localhost");
    }

    private static int defaultPort() {
        String port = System.getProperty("inventory.mongo.port", "27017");
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return 27017;
        }
    }

    public void save(Item item) {
        Document doc = new Document("id", item.getId())
                .append("name", item.getName())
                .append("quantity", item.getQuantity())
                .append("price", item.getPrice())
                .append("description", item.getDescription());
        collection.insertOne(doc);
    }

    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        for (Document doc : collection.find()) {
            Item item = new Item(
                    doc.getString("id"),
                    doc.getString("name"),
                    doc.getInteger("quantity"),
                    doc.getDouble("price"),
                    doc.getString("description")
            );
            items.add(item);
        }
        return items;
    }

    public Optional<Item> findById(String id) {
        Document doc = collection.find(new Document("id", id)).first();
        if (doc == null) return Optional.empty();
        Item item = new Item(
                doc.getString("id"),
                doc.getString("name"),
                doc.getInteger("quantity"),
                doc.getDouble("price"),
                doc.getString("description")
        );
        return Optional.of(item);
    }

    public void update(Item item) {
        Document update = new Document("$set", new Document()
                .append("name", item.getName())
                .append("quantity", item.getQuantity())
                .append("price", item.getPrice())
                .append("description", item.getDescription()));
        collection.updateOne(new Document("id", item.getId()), update);
    }

    public void delete(String id) {
        collection.deleteOne(new Document("id", id));
    }
}


