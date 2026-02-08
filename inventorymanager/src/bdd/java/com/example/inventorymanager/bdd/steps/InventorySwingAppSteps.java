package com.example.inventorymanager.bdd.steps;

import static com.example.inventorymanager.bdd.steps.DatabaseSteps.COLLECTION_NAME;
import static com.example.inventorymanager.bdd.steps.DatabaseSteps.DB_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;

import com.example.inventorymanager.bdd.InventorySwingAppBDD;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class InventorySwingAppSteps {


	private FrameFixture window;

	@After
	public void tearDown() {
		if (window != null)
			window.cleanUp();
	}

	@When("The Inventory View is shown")
	public void the_Inventory_View_is_shown() {
		// start the Swing application
		application("com.example.inventorymanager.app.swing.InventorySwingApp")
			.withArgs(
				"--mongo-port=" + InventorySwingAppBDD.mongoPort,
				"--db-name=" + DB_NAME,
				"--db-collection=" + COLLECTION_NAME
			)
			.start();
		// get a reference of its JFrame
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Inventory Manager".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(BasicRobot.robotWithCurrentAwtHierarchy());
	}

	@Then("The list contains elements with the following values")
	public void the_list_contains_elements_with_the_following_values(List<List<String>> values) {
		values.forEach(
			v -> assertThat(window.list().contents())
				.anySatisfy(e -> assertThat(e).contains(v.get(0), v.get(1) , v.get(2) , v.get(3),v.get(4)))
		);
	}

	@When("The user enters the following values in the text fields")
	public void the_user_enters_the_following_values_in_the_text_fields(List<Map<String, String>> values) {
		values
			.stream()
			.flatMap(m -> m.entrySet().stream())
			.forEach(
				e -> window
					.textBox(e.getKey() + "TextBox")
					.enterText(e.getValue())
			);
	}

	@When("The user clicks the {string} button")
	public void the_user_clicks_the_button(String buttonText) {
		window.button(JButtonMatcher.withText(buttonText)).click();
	}

	@Then("An error is shown containing the following values")
	public void an_error_is_shown_containing_the_following_values(List<List<String>> values) {
		assertThat(window.label("errorMessageLabel").text())
			.contains(values.get(0));
	}

	@Given("The user provides item data in the text fields")
	public void the_user_provides_item_data_in_the_text_fields() {
		window.textBox("idTextBox").enterText("1");
	    window.textBox("nameTextBox").enterText("Laptop");
	    window.textBox("quantityTextBox").enterText("10");
	    window.textBox("priceTextBox").enterText("999.99");
	    window.textBox("descriptionTextBox").enterText("Gaming Laptop");
	}

	@Then("The list contains the new item")
	public void the_list_contains_the_new_item() {
		assertThat(window.list().contents())
			.anySatisfy(e -> assertThat(e).contains("1", "Laptop" , "10" , "999.9", "Gaming Laptop"));
	}

	@Given("The user provides item data in the text fields, specifying an existing id")
	public void the_user_provides_item_data_in_the_text_fields_specifying_an_existing_id() {
		window.textBox("idTextBox").enterText(DatabaseSteps.ITEM_FIXTURE_1_ID);
		window.textBox("nameTextBox").enterText("New Laptop");
	    window.textBox("quantityTextBox").enterText("10");
	    window.textBox("priceTextBox").enterText("999.99");
	    window.textBox("descriptionTextBox").enterText("Laptop");
	}

	@Then("An error is shown containing the name of the existing item")
	public void an_error_is_shown_containing_the_name_of_the_existing_item() {
		assertThat(window.label("errorMessageLabel").text())
			.contains(DatabaseSteps.ITEM_FIXTURE_1_NAME);
	}
	
	@Given("The user selects an item from the list")
	public void the_user_selects_an_item_from_the_list()  {
		window.list("itemList")
			.selectItem(Pattern.compile(".*" + DatabaseSteps.ITEM_FIXTURE_1_NAME + ".*"));
	}

	@Then("The item is removed from the list")
	public void the_item_is_removed_from_the_list() {
		assertThat(window.list().contents())
			.noneMatch(e -> e.contains(".*" + DatabaseSteps.ITEM_FIXTURE_1_NAME + ".*"));
	}

	@Then("An error is shown containing the name of the selected item")
	public void an_error_is_shown_containing_the_name_of_the_selected_item() {
		assertThat(window.label("errorMessageLabel").text())
			.contains(DatabaseSteps.ITEM_FIXTURE_1_NAME);
	}
	
	
	@Then("The list contains the new inventory item")
	public void the_list_contains_the_new_inventory_item() {
	    // Check that the list contains the newly added item
	    assertThat(window.list().contents())
	        .anySatisfy(e -> assertThat(e).contains("1", "Laptop", "10", "999.9", "Simple Laptop"));
	}

	@Given("The user selects the item with id {string} from the list")
	public void the_user_selects_the_item_with_id_from_the_list(String itemId) {
	    // Select the item with the specified ID from the list
	    window.list("itemList")
	        .selectItem(Pattern.compile(".*" + itemId + ".*"));
	}

	@When("The user updates the item details with the following values")
	public void the_user_updates_the_item_details_with_the_following_values(List<Map<String, String>> values) {
	    // Clear and enter new values for each text field
	    Map<String, String> itemDetails = values.get(0);
	
	    window.textBox("nameTextBox").setText(itemDetails.get("name"));
	    window.textBox("quantityTextBox").setText(itemDetails.get("quantity"));
	    window.textBox("priceTextBox").setText(itemDetails.get("price"));
	    window.textBox("descriptionTextBox").setText(itemDetails.get("description"));
	    
	    // Click the update button
	    window.button(JButtonMatcher.withText("Update Selected")).click();
	}

	@Then("The list reflects the updated details for the item with id {string}")
	public void the_list_reflects_the_updated_details_for_the_item_with_id(String itemId) {
	    // Verify that the list contains the updated details for the specified item ID
	    assertThat(window.list().contents())
	        .anySatisfy(e -> assertThat(e).contains(itemId, "Updated Laptop", "15", "899.9", "High-performance laptop"));
	}

}
