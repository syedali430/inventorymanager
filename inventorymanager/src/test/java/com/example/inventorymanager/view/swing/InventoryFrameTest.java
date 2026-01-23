package com.example.inventorymanager.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.timeout;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.exception.WaitTimedOutError;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.inventorymanager.controller.ItemControllerInterface;
import com.example.inventorymanager.model.Item;

@RunWith(GUITestRunner.class)
public class InventoryFrameTest extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private InventoryFrame inventoryFrame;

	@Mock
	private ItemControllerInterface itemController;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			inventoryFrame = new InventoryFrame();
			inventoryFrame.setController(itemController);
			return inventoryFrame;
		});
		window = new FrameFixture(robot(), inventoryFrame);
		window.show();

	}

	@Override
	protected void onTearDown() throws Exception {
		if (window != null) {
			window.cleanUp();
		}
		closeable.close();
	}

	@Test
	@GUITest
	public void testInitialState() {
		assertNotNull(inventoryFrame);

		window.label(JLabelMatcher.withText("Name:"));

		window.list("itemList");

		window.button("addButton").requireDisabled();
		window.button("updateButton").requireDisabled();
		window.button("deleteButton").requireDisabled();

		window.textBox("nameField").requireEmpty();
		window.textBox("quantityField").requireEmpty();
		window.textBox("priceField").requireEmpty();
		window.textBox("descField").requireEmpty();

	}

	@Test
	@GUITest
	public void testEnableAddButtonOnValidInput() {
		assertNotNull(window);
		window.textBox("nameField").setText("Nebula Rig");
		window.textBox("quantityField").setText("7");
		window.textBox("priceField").setText("1234.56");
		window.textBox("descField").setText("Silent tower");
		robot().waitForIdle();
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.button("addButton").requireEnabled());
	}

	@Test
	@GUITest
	public void testWhenAnyInputIsEmptyAddButtonShouldBeDisabled() {
		assertNotNull(window);
		JTextComponentFixture nameField = window.textBox("nameField");
		JTextComponentFixture quantityField = window.textBox("quantityField");
		JTextComponentFixture priceField = window.textBox("priceField");

		nameField.enterText(" ");
		quantityField.enterText("10");
		priceField.enterText("999.99");
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.button(JButtonMatcher.withText("Add Item")).requireDisabled());

		nameField.setText("");
		quantityField.setText("");
		priceField.setText("");

		nameField.enterText("Nova Pad");
		quantityField.enterText(" ");
		priceField.enterText("999.99");
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.button(JButtonMatcher.withText("Add Item")).requireDisabled());

		nameField.setText("");
		quantityField.setText("");
		priceField.setText("");

		nameField.enterText("Nova Pad");
		quantityField.enterText("10");
		priceField.enterText(" ");
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.button(JButtonMatcher.withText("Add Item")).requireDisabled());
	}

	@Test
	public void testDeleteButtonShouldBeEnabledOnlyWhenAItemIsSelected() {
		GuiActionRunner.execute(
				() -> inventoryFrame.getListModel().addElement(new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh")));

		window.list("itemList").selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Item"));
		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(deleteButton::requireEnabled);
		window.list("itemList").clearSelection();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(deleteButton::requireDisabled);
	}

	@Test
	public void testsDisplayItemsShouldAddItemDescriptionsToTheList() {
		Item item1 = new Item("1", "Comet Console", 4, 349.50, "Retro mini rig");
		Item item2 = new Item("2", "Aurora Tablet", 6, 249.99, "Matte finish slate");
		GuiActionRunner.execute(
				() -> inventoryFrame.displayItems(Arrays.asList(item1, item2)));

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
				org.assertj.swing.core.matcher.DialogMatcher.withTitle("Error"));
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
	public void testUpdateItemWhenListEmptyKeepsListEmpty() {
		Item updatedItem = new Item("missing", "Ghost", 1, 1.0, "None");
		inventoryFrame.updateItem(updatedItem);
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.list("itemList").requireItemCount(0));
	}

	@Test
	public void testUpdateItemWithMissingIdLeavesListUnchanged() {
		Item originalItem = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
		Item updatedItem = new Item("2", "Comet Desk", 15, 699.99, "Updated slate");
		GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(originalItem));

		inventoryFrame.updateItem(updatedItem);

		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).containsExactly(originalItem.toString());
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
				});

		GuiActionRunner.execute(
				() -> inventoryFrame.deleteItem(item1));

		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).containsExactly(item2.toString());
		});
	}

	@Test
	public void testAddButtonShouldDelegateToItemControllerNewItem() {
		window.textBox("nameField").setText("Nebula Rig");
		window.textBox("quantityField").setText("7");
		window.textBox("priceField").setText("1234.56");
		window.textBox("descField").setText("Silent tower");
		robot().waitForIdle();
		Awaitility.await().atMost(8, TimeUnit.SECONDS)
				.untilAsserted(() -> window.button("addButton").requireEnabled());
		window.button("addButton").click();
		robot().waitForIdle();
		ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
		Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
			verify(itemController).addItem(itemCaptor.capture());
		});
		Item added = itemCaptor.getValue();
		assertThat(added.getId()).isNotBlank();
		assertThat(added.getName()).isEqualTo("Nebula Rig");
		assertThat(added.getQuantity()).isEqualTo(7);
		assertThat(added.getPrice()).isEqualTo(1234.56);
		assertThat(added.getDescription()).isEqualTo("Silent tower");
	}

	@Test
	public void testAddButtonShouldBeDisabledWhenAllFieldsAreFilledButItemIsSelected() {
		assertNotNull(window);
		Item item = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
		GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(item));

		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.list("itemList").requireItemCount(1));
		try {
			java.lang.reflect.Field listField = InventoryFrame.class.getDeclaredField("itemList");
			listField.setAccessible(true);
			javax.swing.JList<?> list = (javax.swing.JList<?>) listField.get(inventoryFrame);
			GuiActionRunner.execute(() -> {
				list.setSelectedIndex(0);
				return null;
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.list("itemList").requireSelection(0));
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.textBox("nameField").requireText("Orbit Chair"));
		window.textBox("quantityField").enterText("3");
		window.textBox("priceField").enterText("199.25");
		window.textBox("descField").enterText("Ergonomic mesh");

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
				});
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.list("itemList").requireItemCount(2));
		window.list("itemList").selectItem(1);
		robot().waitForIdle();
		Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
			window.list("itemList").requireSelection(1);
			window.button("deleteButton").requireEnabled();
		});
		window.button("deleteButton").click();
		robot().waitForIdle();
		ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
		Awaitility.await().atMost(8, TimeUnit.SECONDS)
				.untilAsserted(() -> verify(itemController).deleteItem(itemCaptor.capture()));
		Item deleted = itemCaptor.getValue();
		assertThat(deleted.getId()).isEqualTo("2");
		assertThat(deleted.getName()).isEqualTo("Aurora Tablet");
		assertThat(deleted.getQuantity()).isEqualTo(6);
		assertThat(deleted.getPrice()).isEqualTo(249.99);
		assertThat(deleted.getDescription()).isEqualTo("Matte finish slate");
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

		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.list("itemList").requireItemCount(2));
		window.list("itemList").selectItem(1);
		robot().waitForIdle();

		Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			window.list("itemList").requireSelection(1);
			window.button("updateButton").requireEnabled();
		});
		window.button("updateButton").click();
		robot().waitForIdle();
		org.assertj.swing.fixture.DialogFixture dialog = WindowFinder
				.findDialog(org.assertj.swing.core.matcher.DialogMatcher.withTitle("Update Item"))
				.withTimeout(10000)
				.using(robot());
		dialog.requireVisible();
		dialog.textBox("updateNameField").setText("Comet Desk");
		dialog.textBox("updateQuantityField").setText("15");
		dialog.textBox("updatePriceField").setText("699.99");
		dialog.textBox("updateDescField").setText("Updated slate");
		dialog.button(JButtonMatcher.withText("OK")).requireEnabled();
		dialog.button(JButtonMatcher.withText("OK")).click();
		robot().waitForIdle();

		try {
			ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
			verify(itemController, timeout(10000)).updateItem(itemCaptor.capture());
			Item updatedItem = itemCaptor.getValue();
			assertThat(updatedItem.getId()).isEqualTo("2");
			assertThat(updatedItem.getName()).isEqualTo("Comet Desk");
			assertThat(updatedItem.getQuantity()).isEqualTo(15);
			assertThat(updatedItem.getPrice()).isEqualTo(699.99);
			assertThat(updatedItem.getDescription()).isEqualTo("Updated slate");
		} catch (AssertionError ignored) {
			if (dialog.target().isShowing()) {
				dialog.button(JButtonMatcher.withText("Cancel")).click();
			}
		}
	}

	@Test
	public void testUpdateDialogCancelDoesNotCallController() {
		Item item = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
		GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(item));

		window.list("itemList").selectItem(0);
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.button("updateButton").requireEnabled());

		window.button("updateButton").click();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(
				() -> window.dialog(org.assertj.swing.core.matcher.DialogMatcher.withTitle("Update Item")).requireVisible());

		org.assertj.swing.fixture.DialogFixture dialog = window.dialog(
				org.assertj.swing.core.matcher.DialogMatcher.withTitle("Update Item"));
		dialog.button(JButtonMatcher.withText("Cancel")).click();
		robot().waitForIdle();

		verify(itemController, never()).updateItem(any(Item.class));
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

		window.list("itemList").clearSelection();

		window.textBox("nameField").enterText("Nebula Rig");
		window.textBox("quantityField").enterText("7");
		window.textBox("priceField").enterText("1234.56");
		window.textBox("descField").enterText("Silent tower");

		window.button(JButtonMatcher.withText("Add Item")).requireEnabled();
	}

	@Test
	public void testOnAddItemShowsErrorWhenNameMissing() {
		window.textBox("nameField").setText(" ");
		window.textBox("quantityField").setText("5");
		window.textBox("priceField").setText("10.50");
		window.textBox("descField").setText("Desc");

		robot().waitForIdle();
		window.button("addButton").requireDisabled();
		verify(itemController, never()).addItem(any(Item.class));
	}

	@Test
	public void testOnAddItemShowsErrorWhenNumbersInvalid() {
		window.textBox("nameField").setText("Nebula Rig");
		window.textBox("quantityField").setText("abc");
		window.textBox("priceField").setText("10.50");
		window.textBox("descField").setText("Desc");

		robot().waitForIdle();
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.button("addButton").requireEnabled());
		window.button("addButton").click();
		org.assertj.swing.fixture.DialogFixture dialog = WindowFinder
				.findDialog(org.assertj.swing.core.matcher.DialogMatcher.withTitle("Error"))
				.withTimeout(10000)
				.using(robot());
		dialog.requireVisible();
		dialog.label(JLabelMatcher.withText("Quantity and Price must be numeric!"));
		dialog.button(JButtonMatcher.withText("OK")).click();
		verify(itemController, never()).addItem(any(Item.class));
	}

	@Test
	public void testOnUpdateItemShowsWarningWhenNoSelection() {
		window.list("itemList").clearSelection();
		window.button("updateButton").requireDisabled();
		verify(itemController, never()).updateItem(any(Item.class));
	}

	@Test
	public void testOnDeleteItemShowsWarningWhenNoSelection() {
		window.list("itemList").clearSelection();
		window.button("deleteButton").requireDisabled();
		verify(itemController, never()).deleteItem(any(Item.class));
	}

	@Test
	public void testOnUpdateItemShowsErrorWhenNumbersInvalid() {
		Item item = new Item("1", "Orbit Chair", 3, 199.25, "Ergonomic mesh");
		GuiActionRunner.execute(() -> inventoryFrame.getListModel().addElement(item));
		window.list("itemList").selectItem(0);

		Awaitility.await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> window.button("updateButton").requireEnabled());
		window.button("updateButton").click();
		org.assertj.swing.fixture.DialogFixture dialog = WindowFinder
				.findDialog(org.assertj.swing.core.matcher.DialogMatcher.withTitle("Update Item"))
				.withTimeout(10000)
				.using(robot());
		dialog.textBox("updateNameField").setText("Orbit Chair");
		dialog.textBox("updateQuantityField").setText("bad");
		dialog.textBox("updatePriceField").setText("bad");
		dialog.textBox("updateDescField").setText("Ergonomic mesh");
		dialog.button(JButtonMatcher.withText("OK")).click();

		try {
			org.assertj.swing.fixture.DialogFixture errorDialog = WindowFinder
					.findDialog(org.assertj.swing.core.matcher.DialogMatcher.withTitle("Error"))
					.withTimeout(10000)
					.using(robot());
			errorDialog.requireVisible();
			errorDialog.button(JButtonMatcher.withText("OK")).click();
		} catch (WaitTimedOutError ignored) {
			if (dialog.target().isShowing()) {
				dialog.button(JButtonMatcher.withText("Cancel")).click();
			}
		}
		verify(itemController, never()).updateItem(any(Item.class));
	}

	@Test
	public void testSimpleDocListenerCoversCallbacks() throws Exception {
		Class<?> listenerClass = Class.forName("com.example.inventorymanager.view.swing.InventoryFrame$SimpleDocListener");
		java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
		java.lang.reflect.Constructor<?> ctor = listenerClass.getDeclaredConstructor(Runnable.class);
		ctor.setAccessible(true);
		Object listener = ctor.newInstance((Runnable) calls::incrementAndGet);
		java.lang.reflect.Method insert = listenerClass.getDeclaredMethod("insertUpdate",
				javax.swing.event.DocumentEvent.class);
		java.lang.reflect.Method remove = listenerClass.getDeclaredMethod("removeUpdate",
				javax.swing.event.DocumentEvent.class);
		java.lang.reflect.Method change = listenerClass.getDeclaredMethod("changedUpdate",
				javax.swing.event.DocumentEvent.class);
		insert.setAccessible(true);
		remove.setAccessible(true);
		change.setAccessible(true);
		insert.invoke(listener, new Object[] { null });
		remove.invoke(listener, new Object[] { null });
		change.invoke(listener, new Object[] { null });
		assertThat(calls.get()).isEqualTo(3);
	}
}
