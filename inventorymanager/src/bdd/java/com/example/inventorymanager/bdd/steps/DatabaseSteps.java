package com.example.inventorymanager.bdd.steps;

import java.util.List;

import org.bson.Document;

import com.example.inventorymanager.bdd.InventorySwingAppBDD;
import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

public class DatabaseSteps {

	static final String DB_NAME = "test-db";
	static final String COLLECTION_NAME = "test-collection";

	static final String ITEM_FIXTURE_1_ID = "1";
	static final String ITEM_FIXTURE_1_NAME = "Laptop";
	static final int ITEM_FIXTURE_1_QUANTITY = 10;
	static final double ITEM_FIXTURE_1_PRICE = 999.9;
	static final String ITEM_FIXTURE_1_DESCRIPTION = "Simple Laptop";
	static final String ITEM_FIXTURE_2_ID = "2";
	static final String ITEM_FIXTURE_2_NAME = "Mobile";
	static final int ITEM_FIXTURE_2_QUANTITY = 5;
	static final double ITEM_FIXTURE_2_PRICE = 899.9;
	static final String ITEM_FIXTURE_2_DESCRIPTION = "Gaming Phone";

	private MongoClient mongoClient;

	@Before
	public void setUp() {
		mongoClient = new MongoClient("localhost", InventorySwingAppBDD.mongoPort);
		// always start with an empty database
		mongoClient.getDatabase(DB_NAME).drop();
	}

	@After
	public void tearDown() {
		mongoClient.close();
	}

	@Given("The database contains the items with the following values")
	public void the_database_contains_the_items_with_the_following_values(List<List<String>> values) {
	    // Skip the header row (assuming the first row contains headers)
	    values.stream().skip(1).forEach(
	        v -> addTestItemToDatabase(v.get(0), v.get(1), Integer.parseInt(v.get(2)), Double.parseDouble(v.get(3)), v.get(4))
	    );
	}

	@Given("The database contains a few items")
	public void the_database_contains_a_few_items() {
		addTestItemToDatabase(ITEM_FIXTURE_1_ID, ITEM_FIXTURE_1_NAME, ITEM_FIXTURE_1_QUANTITY, ITEM_FIXTURE_1_PRICE,
				ITEM_FIXTURE_1_DESCRIPTION);
		addTestItemToDatabase(ITEM_FIXTURE_2_ID, ITEM_FIXTURE_2_NAME, ITEM_FIXTURE_2_QUANTITY, ITEM_FIXTURE_2_PRICE,
				ITEM_FIXTURE_2_DESCRIPTION);
	}

	@Given("The item is in the meantime removed from the database")
	public void the_item_is_in_the_meantime_removed_from_the_database() {
		mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME).deleteOne(Filters.eq("id", ITEM_FIXTURE_1_ID));
	}

	private void addTestItemToDatabase(String id, String name, int quantity, double price, String description) {
		mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME).insertOne(new Document()

				.append("id", id).append("name", name).append("quantity", quantity).append("price", price)
				.append("description", description));
	}

}
