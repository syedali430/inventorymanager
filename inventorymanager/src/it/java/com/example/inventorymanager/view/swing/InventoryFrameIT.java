package com.example.inventorymanager.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

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

import com.example.inventorymanager.controller.ItemControllerInterface;
import com.example.inventorymanager.guice.InventorySwingMongoModule;
import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.ItemRepositoryInterface;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.mongodb.MongoClient;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

@RunWith(GUITestRunner.class)
public class InventoryFrameIT extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private InventoryFrame inventoryFrame;
	private ItemControllerInterface itemController;
	private ItemRepositoryInterface itemRepository;
	private MongoClient mongoClient;
	private static MongoServer mongoServer;
	private static int mongoPort;

	@org.junit.BeforeClass
	public static void startMongo() {
		mongoServer = new MongoServer(new MemoryBackend());
		mongoServer.bind("localhost", 0);
		mongoPort = mongoServer.getLocalAddress().getPort();
	}

	@org.junit.AfterClass
	public static void stopMongo() {
		if (mongoServer != null) {
			mongoServer.shutdownNow();
		}
	}

	@Override
	protected void onSetUp() throws Exception {
		Module moduleForTesting = Modules.override(
				new InventorySwingMongoModule()
					.mongoHost("localhost")
					.mongoPort(mongoPort)
					.databaseName("inventorydb")
					.collectionName("items")
			).with(new AbstractModule() {
				@Override
				protected void configure() {
					bind(MongoClient.class).toInstance(new MongoClient("localhost", mongoPort));
				}
			});

		Injector injector = Guice.createInjector(moduleForTesting);
		mongoClient = injector.getInstance(MongoClient.class);
		itemRepository = injector.getInstance(ItemRepositoryInterface.class);

		GuiActionRunner.execute(() -> {
			inventoryFrame = injector.getInstance(InventoryFrame.class);
			return inventoryFrame;
		});
		mongoClient.getDatabase("inventorydb").drop();
		itemController = inventoryFrame.getController();
		window = new FrameFixture(robot(), inventoryFrame);
		window.show();
		itemController.getAllItems();
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
			.untilAsserted(() -> window.list("itemList").requireItemCount(0));
	}

	@Override
	protected void onTearDown() throws Exception {
		if (window != null) {
			window.cleanUp();
		}
		if (mongoClient != null) {
			mongoClient.close();
		}
		super.onTearDown();
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
		Item newItem = new Item("1", "Laptop", 10, 999.99, "Gaming Laptop");
		itemController.addItem(newItem);

		Awaitility.await().atMost(10, TimeUnit.SECONDS)
			.untilAsserted(() -> assertThat(itemRepository.findAll()).hasSize(1));
		Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			int size = GuiActionRunner.execute(() -> inventoryFrame.getListModel().getSize());
			assertThat(size).isEqualTo(1);
		});
		Item savedItem = GuiActionRunner.execute(() -> inventoryFrame.getListModel().getElementAt(0));
		assertThat(savedItem).isEqualTo(newItem);
	}

	@Test @GUITest
	public void testDeleteItem() {

		Item seeded = new Item("1", "Laptop", 10, 999.99, "Gaming Laptop");
		itemRepository.save(seeded);
		itemController.getAllItems();

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

		Item seeded = new Item("1", "Laptop", 10, 999.99, "Gaming Laptop");
		itemRepository.save(seeded);
		itemController.getAllItems();

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

	private void fillItemForm(String name, String quantity, String price, String description) {
		window.textBox("nameField").setText(name);
		window.textBox("quantityField").setText(quantity);
		window.textBox("priceField").setText(price);
		window.textBox("descField").setText(description);
		robot().waitForIdle();
		Awaitility.await().atMost(5, TimeUnit.SECONDS)
			.untilAsserted(() -> window.button("addButton").requireEnabled());
	}
}
