package com.example.inventorymanager.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.ItemRepositoryInterface;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class ItemMongoRepository implements ItemRepositoryInterface {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_QUANTITY = "quantity";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_DESCRIPTION = "description";

    private final MongoCollection<Document> itemCollection;

    public ItemMongoRepository(MongoClient client, String databaseName, String collectionName) {
        itemCollection = client.getDatabase(databaseName).getCollection(collectionName);
    }

    @Override
    public void save(Item item) {
        itemCollection.insertOne(new Document()
                .append(FIELD_ID, item.getId())
                .append(FIELD_NAME, item.getName())
                .append(FIELD_QUANTITY, item.getQuantity())
                .append(FIELD_PRICE, item.getPrice())
                .append(FIELD_DESCRIPTION, item.getDescription()));
    }

    @Override
    public List<Item> findAll() {
        return StreamSupport.stream(itemCollection.find().spliterator(), false)
                .map(this::fromDocumentToItem)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(String id) {
        Document document = itemCollection.find(Filters.eq(FIELD_ID, id)).first();
        return Optional.ofNullable(document).map(this::fromDocumentToItem);
    }

    @Override
    public void update(Item item) {
        Document updatedDocument = new Document()
                .append(FIELD_NAME, item.getName())
                .append(FIELD_QUANTITY, item.getQuantity())
                .append(FIELD_PRICE, item.getPrice())
                .append(FIELD_DESCRIPTION, item.getDescription());

        itemCollection.updateOne(Filters.eq(FIELD_ID, item.getId()), new Document("$set", updatedDocument));
    }

    @Override
    public void delete(String id) {
        itemCollection.deleteOne(Filters.eq(FIELD_ID, id));
    }

    private Item fromDocumentToItem(Document document) {
        return new Item(
                String.valueOf(document.get(FIELD_ID)),
                String.valueOf(document.get(FIELD_NAME)),
                ((Number) document.get(FIELD_QUANTITY)).intValue(),
                ((Number) document.get(FIELD_PRICE)).doubleValue(),
                String.valueOf(document.get(FIELD_DESCRIPTION))
        );
    }
}
