package com.example.inventorymanager.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.DialogMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.inventorymanager.controller.ItemController;
import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.ItemRepository;
import com.example.inventorymanager.repository.ItemRepositoryInterface;

@RunWith(GUITestRunner.class)
public class InventoryFrameIT extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private InventoryFrame inventoryFrame;
	private ItemController itemController;
	private ItemRepositoryInterface itemRepository;

	@Override
	protected void onSetUp() throws Exception {
		itemRepository = ItemRepository.createDefault();

		for (Item item : itemRepository.findAll()) {
			itemRepository.delete(item.getId());
		}

		GuiActionRunner.execute(() -> {
			inventoryFrame = new InventoryFrame();
			itemController = new ItemController(itemRepository, inventoryFrame);
			inventoryFrame.setController(itemController);
			return inventoryFrame;
		});
		window = new FrameFixture(robot(), inventoryFrame);
		window.show();
		itemController.getAllItems();
	}

	@Test @GUITest
	public void testOnStartAllItemsAreLoaded() {

		Item item1 = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
		Item item2 = new Item("2", "Mouse", 5, 29.99, "Wireless mouse");
		itemRepository.save(item1);
		itemRepository.save(item2);

		itemController.getAllItems();
		Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).containsExactly(item1.toString(), item2.toString());
		});
	}

	@Test @GUITest
	public void testAddItem() {
		window.textBox("nameField").enterText("Laptop");
		window.textBox("quantityField").enterText("10");
		window.textBox("priceField").enterText("999.99");
		window.textBox("descField").enterText("Gaming Laptop");
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
			.untilAsserted(() -> window.button("addButton").requireEnabled());
		window.button("addButton").click();

		Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).hasSize(1);

			assertThat(listContents[0]).contains("Laptop").contains("10").contains("999.99");
		});
	}

	@Test @GUITest
	public void testDeleteItem() {

		window.textBox("nameField").enterText("Laptop");
		window.textBox("quantityField").enterText("10");
		window.textBox("priceField").enterText("999.99");
		window.textBox("descField").enterText("Gaming Laptop");
		window.button("addButton").click();

		Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).hasSize(1);
		});


		window.list("itemList").selectItem(0);
		window.button("deleteButton").click();

		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).isEmpty();
		});
	}

	@Test @GUITest
	public void testUpdateItem() {

		window.textBox("nameField").enterText("Laptop");
		window.textBox("quantityField").enterText("10");
		window.textBox("priceField").enterText("999.99");
		window.textBox("descField").enterText("Gaming Laptop");
		window.button("addButton").click();

		Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).hasSize(1);
		});

		window.list("itemList").selectItem(0);
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
			.untilAsserted(() -> window.button("updateButton").requireEnabled());
		window.button("updateButton").click();

		DialogFixture dialog = window.dialog(DialogMatcher.withTitle("Update Item"));
		dialog.requireVisible();
		dialog.textBox("updateNameField").setText("Updated Laptop");
		dialog.textBox("updateQuantityField").setText("15");
		dialog.textBox("updatePriceField").setText("899.99");
		dialog.textBox("updateDescField").setText("Updated gaming laptop");
		dialog.button(JButtonMatcher.withText("OK")).click();

		Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).hasSize(1);
			assertThat(listContents[0]).contains("Updated Laptop").contains("15").contains("899.99");
		});
	}
}
