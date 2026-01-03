package com.example.inventorymanager.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.inventorymanager.app.InventoryApplication;

@RunWith(GUITestRunner.class)
public class InventoryFrameE2E extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private InventoryFrame inventoryFrame;

	@Override
	protected void onSetUp() throws Exception {
		GuiActionRunner.execute(() -> {
			inventoryFrame = new InventoryApplication().createFrame();
			return inventoryFrame;
		});
		window = new FrameFixture(robot(), inventoryFrame);
		window.show();
	}

	@Override
	protected void onTearDown() throws Exception {
		window.cleanUp();
	}

	@Test @GUITest
	public void testAddItemE2E() {
		window.textBox("nameField").enterText("Laptop");
		window.textBox("quantityField").enterText("10");
		window.textBox("priceField").enterText("999.99");
		window.textBox("descField").enterText("Gaming Laptop");
		window.button("addButton").click();

		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).hasSize(1);
			assertThat(listContents[0]).contains("Laptop").contains("10").contains("999.99").contains("Gaming Laptop");
		});
	}

	@Test @GUITest
	public void testDeleteItemE2E() {
		// First add an item
		window.textBox("nameField").enterText("Laptop");
		window.textBox("quantityField").enterText("10");
		window.textBox("priceField").enterText("999.99");
		window.textBox("descField").enterText("Gaming Laptop");
		window.button("addButton").click();

		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).hasSize(1);
		});

		// Select and delete the item
		window.list("itemList").selectItem(0);
		window.button("deleteButton").click();

		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).isEmpty();
		});
	}

	@Test @GUITest
	public void testUpdateItemE2E() {
		// First add an item
		window.textBox("nameField").enterText("Laptop");
		window.textBox("quantityField").enterText("10");
		window.textBox("priceField").enterText("999.99");
		window.textBox("descField").enterText("Gaming Laptop");
		window.button("addButton").click();

		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).hasSize(1);
		});

		// Select the item and update it
		window.list("itemList").selectItem(0);
		window.textBox("nameField").setText("Updated Laptop");
		window.textBox("quantityField").setText("15");
		window.textBox("priceField").setText("899.99");
		window.textBox("descField").setText("Updated gaming laptop");
		window.button("updateButton").click();

		Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String[] listContents = window.list().contents();
			assertThat(listContents).hasSize(1);
			assertThat(listContents[0]).contains("Updated Laptop").contains("15").contains("899.99").contains("Updated gaming laptop");
		});
	}
}
