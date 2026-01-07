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

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_QUANTITY = "quantity";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_DESCRIPTION = "description";

    public void save(Item item) {
        Document doc = new Document(FIELD_ID, item.getId())
                .append(FIELD_NAME, item.getName())
                .append(FIELD_QUANTITY, item.getQuantity())
                .append(FIELD_PRICE, item.getPrice())
                .append(FIELD_DESCRIPTION, item.getDescription());
        collection.insertOne(doc);
    }

    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        for (Document doc : collection.find()) {
            Item item = new Item(
                    doc.getString(FIELD_ID),
                    doc.getString(FIELD_NAME),
                    doc.getInteger(FIELD_QUANTITY),
                    doc.getDouble(FIELD_PRICE),
                    doc.getString(FIELD_DESCRIPTION)
            );
            items.add(item);
        }
        return items;
    }

    public Optional<Item> findById(String id) {
        Document doc = collection.find(new Document(FIELD_ID, id)).first();
        if (doc == null) return Optional.empty();
        Item item = new Item(
                doc.getString(FIELD_ID),
                doc.getString(FIELD_NAME),
                doc.getInteger(FIELD_QUANTITY),
                doc.getDouble(FIELD_PRICE),
                doc.getString(FIELD_DESCRIPTION)
        );
        return Optional.of(item);
    }

    public void update(Item item) {
        Document update = new Document("$set", new Document()
                .append(FIELD_NAME, item.getName())
                .append(FIELD_QUANTITY, item.getQuantity())
                .append(FIELD_PRICE, item.getPrice())
                .append(FIELD_DESCRIPTION, item.getDescription()));
        collection.updateOne(new Document(FIELD_ID, item.getId()), update);
    }

    public void delete(String id) {
        collection.deleteOne(new Document(FIELD_ID, id));
    }
}

