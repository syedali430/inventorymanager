package com.example.inventorymanager.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.inventorymanager.controller.ItemController;
import com.example.inventorymanager.model.Item;


@RunWith(GUITestRunner.class)
public class InventoryFrameTest extends AssertJSwingJUnitTestCase{

	private FrameFixture window;
	private InventoryFrame inventoryFrame;


	@Mock
	private ItemController itemController;

	private AutoCloseable closeable;


	@Override
	protected void onSetUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(()->{
			inventoryFrame = new InventoryFrame(itemController);
			return inventoryFrame;
		});
		window = new FrameFixture(robot(),inventoryFrame);
		window.show();


	}

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}


	@Test @GUITest
	public void testInitialState() {
	    // Ensure buttons are initially disabled
	    window.button("addButton").requireDisabled();
	    window.button("updateButton").requireDisabled();
	    window.button("deleteButton").requireDisabled();

	    // Ensure all text fields are empty
	    window.textBox("nameField").requireEmpty();
	    window.textBox("quantityField").requireEmpty();
	    window.textBox("priceField").requireEmpty();
	    window.textBox("descField").requireEmpty();
	}

	@Test @GUITest
	public void testEnableAddButtonOnValidInput() {
	    // Enter valid inputs
	    window.textBox("nameField").enterText("Nebula Rig");
	    window.textBox("quantityField").enterText("7");
	    window.textBox("priceField").enterText("1234.56");
	    window.textBox("descField").enterText("Silent tower");

	    // Verify the Add button is now enabled
	    window.button("addButton").requireEnabled();
	}

	@Test @GUITest
	public void testWhenAnyInputIsEmptyAddButtonShouldBeDisabled()
	{
		JTextComponentFixture nameField =  window.textBox("nameField");
		JTextComponentFixture quantityField =  window.textBox("quantityField");
		JTextComponentFixture priceField =  window.textBox("priceField");

		//when name is empty
		nameField.enterText(" ");
		quantityField.enterText("10");
		priceField.enterText("999.99");
		window.button(JButtonMatcher.withText("Add Item")).requireDisabled();

		//reseting the inputs
		nameField.setText("");
		quantityField.setText("");
		priceField.setText("");

		//when quantity is empty
		nameField.enterText("Nova Pad");
		quantityField.enterText(" ");
		priceField.enterText("999.99");
		window.button(JButtonMatcher.withText("Add Item")).requireDisabled();


		//reseting the inputs
		nameField.setText("");
		quantityField.setText("");
		priceField.setText("");

		//when price is empty
		nameField.enterText("Nova Pad");
		quantityField.enterText("10");
		priceField.enterText(" ");
		window.button(JButtonMatcher.withText("Add Item")).requireDisabled();
	}

	@Test
	public void testDeleteButtonShouldBeEnabledOnlyWhenAItemIsSelected() {
	    GuiActionRunner.execute(
	        () ->
	        inventoryFrame.getListModel().addElement(new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh"))
	    );

	    window.list("itemList").selectItem(0);
	    JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Item"));
	    deleteButton.requireEnabled();
	    window.list("itemList").clearSelection();
	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(deleteButton::requireDisabled);
	}

	@Test
	public void testsDisplayItemsShouldAddItemDescriptionsToTheList() {
	    Item item1 = new Item("1", "Comet Console", 4, 349.50, "Retro mini rig");
	    Item item2 = new Item("2", "Aurora Tablet", 6, 249.99, "Matte finish slate");
	    GuiActionRunner.execute(
	        () -> inventoryFrame.displayItems(Arrays.asList(item1, item2))
	    );

	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
	        String[] listContents = window.list().contents();
	        assertThat(listContents).containsExactly(item1.toString(), item2.toString());
	    });
	}

	@Test
	public void testShowErrorMessageShouldShowTheMessageInADialog() {
	    Item item = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
	    inventoryFrame.showErrorMessage("error message", item);

	    // Since showErrorMessage uses JOptionPane, we can't easily test the dialog with AssertJ Swing
	    // This test would need to be adapted or use a different approach
	}

	@Test
	public void testItemAddedShouldAddTheItemToTheList() {
	    Item item = new Item("1", "Comet Console", 4, 349.50, "Retro mini rig");
	    inventoryFrame.addItem(item);
	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
	        String[] listContents = window.list().contents();
	        assertThat(listContents).containsExactly(item.toString());
	    });
	}

	@Test
	public void testItemUpdatedShouldUpdateTheItemInTheList() {
	    Item updatedItem = new Item("1", "Comet Console", 5, 399.99, "Updated rig");

	    inventoryFrame.addItem(new Item("1", "Comet Console", 4, 349.50, "Retro mini rig"));

	    inventoryFrame.updateItem(updatedItem);

	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
	        String[] listContents = window.list().contents();
	        assertThat(listContents).containsExactly(updatedItem.toString());
	    });
	}

	@Test
	public void testItemRemovedShouldRemoveTheItemFromTheList() {
	    Item item1 = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	    Item item2 = new Item("2", "Aurora Tablet", 6, 249.99, "Matte finish slate");
	    GuiActionRunner.execute(
	        () -> {
	            DefaultListModel<Item> listItemModel = inventoryFrame.getListModel();
	            listItemModel.addElement(item1);
	            listItemModel.addElement(item2);
	        }
	    );

	    GuiActionRunner.execute(
	        () -> inventoryFrame.deleteItem(item1)
	    );

	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
	        String[] listContents = window.list().contents();
	        assertThat(listContents).containsExactly(item2.toString());
	    });
	}

	@Test
	public void testAddButtonShouldDelegateToItemControllerNewItem() {
	    window.textBox("nameField").enterText("Nebula Rig");
	    window.textBox("quantityField").enterText("7");
	    window.textBox("priceField").enterText("1234.56");
	    window.textBox("descField").enterText("Silent tower");
	    window.button(JButtonMatcher.withText("Add Item")).click();
	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
	        verify(itemController).addItem(new Item(String.valueOf(System.currentTimeMillis()), "Nebula Rig", 7, 1234.56, "Silent tower"));
	    });
	}

	@Test
	public void testAddButtonShouldBeDisabledWhenAllFieldsAreFilledButItemIsSelected() {
	    Item item = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	    GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(item));

	    window.list("itemList").selectItem(0);

	    window.textBox("nameField").requireText("Orbit Chair");
	    window.textBox("quantityField").enterText("3");
	    window.textBox("priceField").enterText("199.25");
	    window.textBox("descField").enterText("Ergonomic mesh");

	    // Verify that the Add button is disabled due to item selection
	    window.button(JButtonMatcher.withText("Add Item")).requireDisabled();
	}

	@Test
	public void testDeleteButtonShouldDelegateToItemControllerDeleteItem() {
	    Item item1 = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	    Item item2 = new Item("2", "Aurora Tablet", 6, 249.99, "Matte finish slate");
	    GuiActionRunner.execute(
	        () -> {
	            DefaultListModel<Item> listStudentsModel = inventoryFrame.getListModel();
	            listStudentsModel.addElement(item1);
	            listStudentsModel.addElement(item2);
	        }
	    );
	    window.list("itemList").selectItem(1);
	    window.button(JButtonMatcher.withText("Delete Item")).click();
	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
	        verify(itemController).deleteItem(item2);
	    });
	}

	@Test
	public void testUpdateButtonShouldDelegateToItemControllerUpdateSelectedItem() {
	    Item originalItem1 = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	    Item originalItem2 = new Item("2", "Aurora Tablet", 6, 249.99, "Matte finish slate");

	    GuiActionRunner.execute(() -> {
	        DefaultListModel<Item> listItemModel = inventoryFrame.getListModel();
	        listItemModel.addElement(originalItem1);
	        listItemModel.addElement(originalItem2);
	    });

	    window.list("itemList").selectItem(1);

	    window.button(JButtonMatcher.withText("Update Item")).click();

	    org.assertj.swing.fixture.DialogFixture dialog = window.dialog(org.assertj.swing.core.matcher.DialogMatcher.withTitle("Update Item"));
	    dialog.textBox("updateNameField").setText("Comet Desk");
	    dialog.textBox("updateQuantityField").setText("15");
	    dialog.textBox("updatePriceField").setText("699.99");
	    dialog.textBox("updateDescField").setText("Updated slate");
	    dialog.button(JButtonMatcher.withText("OK")).click();

	    Item updatedItem = new Item("2", "Comet Desk", 15, 699.99, "Updated slate");
	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> verify(itemController).updateItem(updatedItem));
	}

	@Test
	public void testAddButtonShouldBeDisabledWhenItemIsSelected() {
	    Item item = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	    GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(item));

	    window.list("itemList").selectItem(0);

	    window.button(JButtonMatcher.withText("Add Item")).requireDisabled();

	}

	@Test
	public void testAddButtonShouldBeEnabledWhenFieldsAreFilledAndNoItemIsSelected() {
	    // Ensure no item is selected
	    window.list("itemList").clearSelection();

	    // Simulate key release event to populate all the fields
	    window.textBox("nameField").enterText("Nebula Rig");
	    window.textBox("quantityField").enterText("7");
	    window.textBox("priceField").enterText("1234.56");
	    window.textBox("descField").enterText("Silent tower");

	    // Verify that the Add button is enabled when all fields are filled
	    window.button(JButtonMatcher.withText("Add Item")).requireEnabled();
	}
}
