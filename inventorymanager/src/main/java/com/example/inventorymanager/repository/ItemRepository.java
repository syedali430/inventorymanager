package com.example.inventorymanager.repository;

import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.guice.MongoCollectionName;
import com.example.inventorymanager.guice.MongoDbName;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemRepository implements ItemRepositoryInterface {

    private static final String DEFAULT_DB_NAME = "inventorydb";
    private static final String DEFAULT_COLLECTION_NAME = "items";

    private final MongoCollection<Document> collection;
    private final MongoClient client;

    @Inject
    public ItemRepository(MongoClient client,
                          @MongoDbName String databaseName,
                          @MongoCollectionName String collectionName) {
        this.client = client;
        MongoDatabase database = client.getDatabase(databaseName);
        collection = database.getCollection(collectionName);
    }

    public static ItemRepository createDefault() {
        return new ItemRepository(new MongoClient(defaultHost(), defaultPort()),
                defaultDatabaseName(), defaultCollectionName());
    }

    public static ItemRepository create(MongoClient client) {
        return new ItemRepository(client, defaultDatabaseName(), defaultCollectionName());
    }

    public static ItemRepository create(String host, int port) {
        return new ItemRepository(new MongoClient(host, port),
                defaultDatabaseName(), defaultCollectionName());
    }

    public static ItemRepository create(String host, int port, String databaseName, String collectionName) {
        return new ItemRepository(new MongoClient(host, port), databaseName, collectionName);
    }

    static String defaultHost() {
        return System.getProperty("mongo.host",
                System.getProperty("inventory.mongo.host", "localhost"));
    }

    static int defaultPort() {
        String port = System.getProperty("mongo.port",
                System.getProperty("inventory.mongo.port", "27017"));
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return 27017;
        }
    }

    private static String defaultDatabaseName() {
        return System.getProperty("inventory.mongo.db", DEFAULT_DB_NAME);
    }

    private static String defaultCollectionName() {
        return System.getProperty("inventory.mongo.collection", DEFAULT_COLLECTION_NAME);
    }

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_QUANTITY = "quantity";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_DESCRIPTION = "description";

    @Override
    public void save(Item item) {
        Document doc = new Document(FIELD_ID, item.getId())
                .append(FIELD_NAME, item.getName())
                .append(FIELD_QUANTITY, item.getQuantity())
                .append(FIELD_PRICE, item.getPrice())
                .append(FIELD_DESCRIPTION, item.getDescription());
        collection.insertOne(doc);
    }

    @Override
    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        collection.find().forEach((Block<Document>) doc -> items.add(toItem(doc)));
        return items;
    }

    @Override
    public Optional<Item> findById(String id) {
        Document doc = collection.find(new Document(FIELD_ID, id)).first();
        return Optional.ofNullable(doc).map(this::toItem);
    }

    @Override
    public void update(Item item) {
        Document update = new Document("$set", new Document()
                .append(FIELD_NAME, item.getName())
                .append(FIELD_QUANTITY, item.getQuantity())
                .append(FIELD_PRICE, item.getPrice())
                .append(FIELD_DESCRIPTION, item.getDescription()));
        collection.updateOne(new Document(FIELD_ID, item.getId()), update);
    }

    @Override
    public void delete(String id) {
        collection.deleteOne(new Document(FIELD_ID, id));
    }

    private Item toItem(Document doc) {
        return new Item(
                doc.getString(FIELD_ID),
                doc.getString(FIELD_NAME),
                doc.getInteger(FIELD_QUANTITY),
                doc.getDouble(FIELD_PRICE),
                doc.getString(FIELD_DESCRIPTION)
        );
    }
}
