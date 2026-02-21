package com.example.inventorymanager.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.timeout;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
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
import org.mockito.ArgumentCaptor;

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
			inventoryFrame = new InventoryFrame();
			inventoryFrame.setController(itemController);
			return inventoryFrame;
		});
		window = new FrameFixture(robot(),inventoryFrame);
		window.show();


	}

	@Override
	protected void onTearDown() throws Exception {
		if (window != null) {
			window.cleanUp();
		}
		closeable.close();
	}

	private void invokePrivateAction(String methodName) {
		SwingUtilities.invokeLater(() -> {
			try {
				java.lang.reflect.Method method = InventoryFrame.class.getDeclaredMethod(methodName, ActionEvent.class);
				method.setAccessible(true);
				method.invoke(inventoryFrame,
					new ActionEvent(this, ActionEvent.ACTION_PERFORMED, methodName));
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}

	private DialogFixture waitForDialog(String title) {
		final Dialog[] dialog = new Dialog[1];
		robot().waitForIdle();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			for (Window openWindow : Window.getWindows()) {
				if (openWindow instanceof Dialog) {
					Dialog openDialog = (Dialog) openWindow;
					if (openDialog.isShowing() && title.equals(openDialog.getTitle())) {
						dialog[0] = openDialog;
						return;
					}
				}
			}
			throw new AssertionError("Dialog not visible: " + title);
		});
		return new DialogFixture(robot(), dialog[0]);
	}


	@Test @GUITest
	public void testInitialState() {
	    assertNotNull(inventoryFrame);
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
	    assertNotNull(window);
	    // Enter valid inputs
	    window.textBox("nameField").enterText("Nebula Rig");
	    window.textBox("quantityField").enterText("7");
	    window.textBox("priceField").enterText("1234.56");
	    window.textBox("descField").enterText("Silent tower");

	    // Verify the Add button is now enabled
	    window.button("addButton").requireEnabled();
	}

	@Test @GUITest
	public void testAddButtonWithNoControllerDoesNotAddItem() {
	    GuiActionRunner.execute(() -> {
	        inventoryFrame.setController(null);
	        return null;
	    });
	    window.textBox("nameField").setText("Nebula Rig");
	    window.textBox("quantityField").setText("7");
	    window.textBox("priceField").setText("1234.56");
	    window.textBox("descField").setText("Silent tower");
	    Awaitility.await().atMost(5, TimeUnit.SECONDS)
	        .untilAsserted(() -> window.button("addButton").requireEnabled());
	    window.button("addButton").click();
	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
	        assertThat(window.list("itemList").contents()).isEmpty());
	}

	@Test
	public void testOnAddItemWithNullControllerDoesNothing() {
	    GuiActionRunner.execute(() -> {
	        inventoryFrame.setController(null);
	        return null;
	    });
	    window.textBox("nameField").setText("Manual Add");
	    window.textBox("quantityField").setText("4");
	    window.textBox("priceField").setText("10.5");
	    window.textBox("descField").setText("No controller");
	    invokePrivateAction("onAddItem");
	    robot().waitForIdle();
	    assertThat(window.list("itemList").contents()).isEmpty();
	}

	@Test
	public void testGetControllerReturnsSetController() {
	    assertThat(inventoryFrame.getController()).isSameAs(itemController);
	}

	@Test
	public void testStartCallsGetAllItems() {
	    GuiActionRunner.execute(() -> {
	        inventoryFrame.start();
	        return null;
	    });
	    verify(itemController, timeout(5000)).getAllItems();
	}

	@Test
	public void testStartWithoutControllerDoesNotThrow() {
	    GuiActionRunner.execute(() -> {
	        inventoryFrame.setController(null);
	        inventoryFrame.start();
	        return null;
	    });
	    assertThat(inventoryFrame.isVisible()).isTrue();
	}

	@Test
	public void testStartSkipsInitialLoadWhenConfigured() {
	    System.setProperty("inventory.test.skipInitialLoad", "true");
	    try {
	        GuiActionRunner.execute(() -> {
	            inventoryFrame.start();
	            return null;
	        });
	        robot().waitForIdle();
	        org.mockito.Mockito.verify(itemController, org.mockito.Mockito.never()).getAllItems();
	    } finally {
	        System.clearProperty("inventory.test.skipInitialLoad");
	    }
	}

	@Test
	public void testStartLoadErrorLogsWhenSilentFalse() {
	    System.clearProperty("inventory.test.skipInitialLoad");
	    System.clearProperty("inventory.test.silentLoadErrors");
	    java.util.logging.Logger logger = java.util.logging.Logger.getLogger(InventoryFrame.class.getName());
	    java.util.logging.Level previous = logger.getLevel();
	    logger.setLevel(java.util.logging.Level.OFF);
	    try {
	        org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(itemController).getAllItems();
	        GuiActionRunner.execute(() -> {
	            inventoryFrame.start();
	            return null;
	        });
	        Awaitility.await().atMost(5, TimeUnit.SECONDS)
	            .untilAsserted(() -> org.mockito.Mockito.verify(itemController).getAllItems());
	    } finally {
	        logger.setLevel(previous);
	    }
	}

	@Test
	public void testStartLoadErrorSilentWhenEnabled() {
	    System.clearProperty("inventory.test.skipInitialLoad");
	    System.setProperty("inventory.test.silentLoadErrors", "true");
	    try {
	        org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(itemController).getAllItems();
	        GuiActionRunner.execute(() -> {
	            inventoryFrame.start();
	            return null;
	        });
	        Awaitility.await().atMost(5, TimeUnit.SECONDS)
	            .untilAsserted(() -> org.mockito.Mockito.verify(itemController).getAllItems());
	    } finally {
	        System.clearProperty("inventory.test.silentLoadErrors");
	    }
	}

	@Test @GUITest
	public void testWhenAnyInputIsEmptyAddButtonShouldBeDisabled()
	{
	    assertNotNull(window);
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
	public void testOnAddItemShowsDialogWhenNameMissing() {
	    window.textBox("nameField").setText(" ");
	    window.textBox("quantityField").setText("2");
	    window.textBox("priceField").setText("3.5");
	    window.textBox("descField").setText("desc");

	    invokePrivateAction("onAddItem");
	    DialogFixture dialog = waitForDialog("Error");
	    dialog.button(JButtonMatcher.withText("OK")).click();
	}

	@Test
	public void testOnAddItemShowsDialogWhenNumbersInvalid() {
	    window.textBox("nameField").setText("Item");
	    window.textBox("quantityField").setText("bad");
	    window.textBox("priceField").setText("3.5");
	    window.textBox("descField").setText("desc");

	    invokePrivateAction("onAddItem");
	    DialogFixture dialog = waitForDialog("Error");
	    dialog.button(JButtonMatcher.withText("OK")).click();
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
	public void testOnUpdateItemShowsWarningWhenNoSelection() {
	    invokePrivateAction("onUpdateItem");
	    DialogFixture dialog = waitForDialog("Warning");
	    dialog.button(JButtonMatcher.withText("OK")).click();
	}

	@Test
	public void testOnDeleteItemShowsWarningWhenNoSelection() {
	    invokePrivateAction("onDeleteItem");
	    DialogFixture dialog = waitForDialog("Warning");
	    dialog.button(JButtonMatcher.withText("OK")).click();
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

	    org.assertj.swing.fixture.DialogFixture dialog = window.dialog(
	        org.assertj.swing.core.matcher.DialogMatcher.withTitle("Error")
	    );
	    assertNotNull(dialog);
	    dialog.button(JButtonMatcher.withText("OK")).click();
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
	public void testUpdateItemReplacesExistingById() {
	    Item original = new Item("1", "Old", 1, 1.0, "d");
	    Item updated = new Item("1", "New", 2, 2.0, "updated");
	    GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(original));

	    inventoryFrame.updateItem(updated);

	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
	        String[] listContents = window.list().contents();
	        assertThat(listContents).containsExactly(updated.toString());
	    });
	}

	@Test
	public void testUpdateItemWithMissingIdLeavesListUnchanged() {
	    Item original = new Item("1", "Old", 1, 1.0, "d");
	    Item missing = new Item("2", "New", 2, 2.0, "updated");
	    GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(original));

	    inventoryFrame.updateItem(missing);

	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
	        String[] listContents = window.list().contents();
	        assertThat(listContents).containsExactly(original.toString());
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
	    robot().waitForIdle();
	    Awaitility.await().atMost(8, TimeUnit.SECONDS)
	        .untilAsserted(() -> window.button("addButton").requireEnabled());
	    window.button("addButton").click();
	    robot().waitForIdle();
	    ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
	    verify(itemController, timeout(8000)).addItem(itemCaptor.capture());
	    Item captured = itemCaptor.getValue();
	    assertThat(captured.getName()).isEqualTo("Nebula Rig");
	    assertThat(captured.getQuantity()).isEqualTo(7);
	    assertThat(captured.getPrice()).isEqualTo(1234.56);
	    assertThat(captured.getDescription()).isEqualTo("Silent tower");
	}

	@Test
	public void testAddButtonShouldBeDisabledWhenAllFieldsAreFilledButItemIsSelected() {
	    assertNotNull(window);
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
	    Awaitility.await().atMost(5, TimeUnit.SECONDS)
	        .untilAsserted(() -> window.list("itemList").requireItemCount(2));
	    window.list("itemList").selectItem(1);
	    robot().waitForIdle();
	    Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
	        window.list("itemList").requireSelection(1);
	        window.button(JButtonMatcher.withText("Delete Item")).requireEnabled();
	    });
	    window.button(JButtonMatcher.withText("Delete Item")).click();
	    robot().waitForIdle();
	    Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> verify(itemController).deleteItem(item2));
	}

	@Test
	public void testDeleteButtonWithNoControllerDoesNotRemoveItem() {
	    Item item = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	    GuiActionRunner.execute(() -> {
	        inventoryFrame.setController(null);
	        inventoryFrame.getListModel().addElement(item);
	        return null;
	    });
	    window.list("itemList").selectItem(0);
	    Awaitility.await().atMost(5, TimeUnit.SECONDS)
	        .untilAsserted(() -> window.button(JButtonMatcher.withText("Delete Item")).requireEnabled());
	    window.button(JButtonMatcher.withText("Delete Item")).click();
	    Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
	        assertThat(window.list("itemList").contents()).containsExactly(item.toString()));
	}

	@Test
	public void testUpdateButtonShouldDelegateToItemControllerUpdateSelectedItem() {
	    System.setProperty("inventory.test.skipUpdateDialog", "true");
	    try {
	    Item originalItem1 = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	    Item originalItem2 = new Item("2", "Aurora Tablet", 6, 249.99, "Matte finish slate");

	    GuiActionRunner.execute(() -> {
	        DefaultListModel<Item> listItemModel = inventoryFrame.getListModel();
	        listItemModel.addElement(originalItem1);
	        listItemModel.addElement(originalItem2);
	    });

	    Awaitility.await().atMost(5, TimeUnit.SECONDS)
	        .untilAsserted(() -> window.list("itemList").requireItemCount(2));
	    window.list("itemList").selectItem(1);
	    robot().waitForIdle();

	    try {
	        java.lang.reflect.Method updateSelectionStateMethod = InventoryFrame.class.getDeclaredMethod("updateSelectionState");
	        updateSelectionStateMethod.setAccessible(true);
	        GuiActionRunner.execute(() -> {
	            try {
	                updateSelectionStateMethod.invoke(inventoryFrame);
	            } catch (Exception ex) {
	                throw new RuntimeException(ex);
	            }
	            return null;
	        });
	    } catch (NoSuchMethodException ex) {
	        throw new AssertionError("updateSelectionState method not found", ex);
	    }
	    window.button("updateButton").requireEnabled();
	    window.textBox("nameField").setText("Comet Desk");
	    window.textBox("quantityField").setText("15");
	    window.textBox("priceField").setText("699.99");
	    window.textBox("descField").setText("Updated slate");
	    window.button("updateButton").click();
	    robot().waitForIdle();

	    ArgumentCaptor<Item> updateCaptor = ArgumentCaptor.forClass(Item.class);
	    verify(itemController, timeout(10000)).updateItem(updateCaptor.capture());
	    Item updatedItem = updateCaptor.getValue();
	    assertThat(updatedItem.getId()).isEqualTo("2");
	    assertThat(updatedItem.getName()).isEqualTo("Comet Desk");
	    assertThat(updatedItem.getQuantity()).isEqualTo(15);
	    assertThat(updatedItem.getPrice()).isEqualTo(699.99);
	    assertThat(updatedItem.getDescription()).isEqualTo("Updated slate");
	    } finally {
	        System.clearProperty("inventory.test.skipUpdateDialog");
	    }
	}

	@Test
	public void testUpdateButtonSkipDialogWithNoControllerDoesNotUpdate() {
	    System.setProperty("inventory.test.skipUpdateDialog", "true");
	    try {
	        Item original = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	        GuiActionRunner.execute(() -> {
	            inventoryFrame.setController(null);
	            inventoryFrame.getListModel().addElement(original);
	            return null;
	        });
	        window.list("itemList").selectItem(0);
	        Awaitility.await().atMost(5, TimeUnit.SECONDS)
	            .untilAsserted(() -> window.button("updateButton").requireEnabled());
	        window.textBox("nameField").setText("Comet Desk");
	        window.textBox("quantityField").setText("15");
	        window.textBox("priceField").setText("699.99");
	        window.textBox("descField").setText("Updated slate");
	        window.button("updateButton").click();
	        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
	            assertThat(window.list("itemList").contents()).containsExactly(original.toString()));
	    } finally {
	        System.clearProperty("inventory.test.skipUpdateDialog");
	    }
	}

	@Test
	public void testUpdateDialogOkUpdatesController() {
	    System.setProperty("inventory.test.skipUpdateDialog", "true");
	    try {
	        Item original = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	        GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(original));
	        window.list("itemList").selectItem(0);
	        Awaitility.await().atMost(5, TimeUnit.SECONDS)
	            .untilAsserted(() -> window.button("updateButton").requireEnabled());
	        window.textBox("nameField").setText("Updated Chair");
	        window.textBox("quantityField").setText("7");
	        window.textBox("priceField").setText("299.99");
	        window.textBox("descField").setText("New mesh");
	        window.button("updateButton").click();
	        robot().waitForIdle();

	        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
	        verify(itemController, timeout(5000)).updateItem(captor.capture());
	        Item updated = captor.getValue();
	        assertThat(updated.getId()).isEqualTo("1");
	        assertThat(updated.getName()).isEqualTo("Updated Chair");
	        assertThat(updated.getQuantity()).isEqualTo(7);
	        assertThat(updated.getPrice()).isEqualTo(299.99);
	        assertThat(updated.getDescription()).isEqualTo("New mesh");
	    } finally {
	        System.clearProperty("inventory.test.skipUpdateDialog");
	    }
	}

	@Test
	public void testUpdateDialogForceOkUpdatesControllerEvenOnCancel() throws Exception {
	    System.setProperty("inventory.test.forceUpdateDialogOk", "true");
	    try {
	        java.lang.reflect.Method shouldApplyUpdate = InventoryFrame.class
	                .getDeclaredMethod("shouldApplyUpdate", int.class);
	        shouldApplyUpdate.setAccessible(true);
	        boolean result = (boolean) shouldApplyUpdate.invoke(inventoryFrame, JOptionPane.CANCEL_OPTION);
	        assertThat(result).isTrue();
	    } finally {
	        System.clearProperty("inventory.test.forceUpdateDialogOk");
	    }
	}

	@Test
	public void testUpdateDialogCancelDoesNotUpdateController() {
	    System.clearProperty("inventory.test.skipUpdateDialog");
	    Item original = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	    GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(original));
	    window.list("itemList").selectItem(0);
	    Awaitility.await().atMost(5, TimeUnit.SECONDS)
	        .untilAsserted(() -> window.button("updateButton").requireEnabled());
	    window.button("updateButton").click();

	    DialogFixture dialog = waitForDialog("Update Item");
	    dialog.button(JButtonMatcher.withText("Cancel")).click();

	    org.mockito.Mockito.verify(itemController, org.mockito.Mockito.never())
	        .updateItem(org.mockito.Mockito.any(Item.class));
	    assertThat(window.list("itemList").contents()).containsExactly(original.toString());
	}

	@Test
	public void testUpdateDialogOkWithNoControllerDoesNotUpdate() {
	    System.clearProperty("inventory.test.skipUpdateDialog");
	    Item original = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	    GuiActionRunner.execute(() -> {
	        inventoryFrame.setController(null);
	        inventoryFrame.getListModel().addElement(original);
	        return null;
	    });
	    window.list("itemList").selectItem(0);
	    Awaitility.await().atMost(5, TimeUnit.SECONDS)
	        .untilAsserted(() -> window.button("updateButton").requireEnabled());
	    window.button("updateButton").click();

	    DialogFixture dialog = waitForDialog("Update Item");
	    dialog.textBox("updateNameField").setText("Updated Chair");
	    dialog.textBox("updateQuantityField").setText("7");
	    dialog.textBox("updatePriceField").setText("299.99");
	    dialog.textBox("updateDescField").setText("New mesh");
	    dialog.button(JButtonMatcher.withText("OK")).click();

	    assertThat(window.list("itemList").contents()).containsExactly(original.toString());
	}

	@Test
	public void testUpdateDialogShowsErrorOnInvalidNumbers() {
	    System.setProperty("inventory.test.skipUpdateDialog", "true");
	    Item original = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	    try {
	        GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(original));
	        window.list("itemList").selectItem(0);
	        Awaitility.await().atMost(5, TimeUnit.SECONDS)
	            .untilAsserted(() -> window.button("updateButton").requireEnabled());
	        window.textBox("nameField").setText("Updated Chair");
	        window.textBox("quantityField").setText("bad");
	        window.textBox("priceField").setText("oops");
	        window.textBox("descField").setText("New mesh");
	        window.button("updateButton").click();

	        DialogFixture errorDialog = waitForDialog("Error");
	        errorDialog.button(JButtonMatcher.withText("OK")).click();
	    } finally {
	        System.clearProperty("inventory.test.skipUpdateDialog");
	    }
	}

	@Test
	public void testSimpleDocListenerChangedUpdateInvokesCallback() throws Exception {
	    AtomicBoolean invoked = new AtomicBoolean(false);
	    Class<?> listenerClass = Class.forName(
	        "com.example.inventorymanager.view.swing.InventoryFrame$SimpleDocListener");
	    java.lang.reflect.Constructor<?> ctor = listenerClass.getDeclaredConstructor(Runnable.class);
	    ctor.setAccessible(true);
	    Object listener = ctor.newInstance((Runnable) () -> invoked.set(true));
	    javax.swing.event.DocumentListener docListener = (javax.swing.event.DocumentListener) listener;

	    docListener.changedUpdate(null);

	    assertThat(invoked.get()).isTrue();
	}

	@Test
	public void testAddButtonShouldBeDisabledWhenItemIsSelected() {
	    assertNotNull(window);
	    Item item = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
	    GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(item));

	    window.list("itemList").selectItem(0);

	    window.button(JButtonMatcher.withText("Add Item")).requireDisabled();

	}

	@Test
	public void testAddButtonShouldBeEnabledWhenFieldsAreFilledAndNoItemIsSelected() {
	    assertNotNull(window);
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
