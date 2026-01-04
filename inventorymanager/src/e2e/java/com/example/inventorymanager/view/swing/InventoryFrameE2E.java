package com.example.inventorymanager.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import com.example.inventorymanager.app.InventoryApplication;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GUITestRunner.class)
public class InventoryFrameE2E extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private InventoryFrame inventoryFrame;
	private MongoServer mongoServer;

	@Override
	protected void onSetUp() throws Exception {
		mongoServer = new MongoServer(new MemoryBackend());
		mongoServer.bind("localhost", 0);
		int port = mongoServer.getLocalAddress().getPort();
		System.setProperty("inventory.mongo.host", "localhost");
		System.setProperty("inventory.mongo.port", String.valueOf(port));

		GuiActionRunner.execute(() -> {
			inventoryFrame = new InventoryApplication().createFrame();
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
		if (mongoServer != null) {
			mongoServer.shutdownNow();
		}
		System.clearProperty("inventory.mongo.host");
		System.clearProperty("inventory.mongo.port");
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
            assertThat(listContents[0]).contains("Laptop").contains("10").contains("999.99");
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

        org.assertj.swing.fixture.DialogFixture dialog = window.dialog(org.assertj.swing.core.matcher.DialogMatcher.withTitle("Update Item"));
        dialog.textBox("updateNameField").setText("Updated Laptop");
        dialog.textBox("updateQuantityField").setText("15");
        dialog.textBox("updatePriceField").setText("899.99");
        dialog.textBox("updateDescField").setText("Updated gaming laptop");
        dialog.button(org.assertj.swing.core.matcher.JButtonMatcher.withText("OK")).click();

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String[] listContents = window.list().contents();
            assertThat(listContents).hasSize(1);
            assertThat(listContents[0]).contains("Updated Laptop").contains("15").contains("899.99");
        });
    }
}
