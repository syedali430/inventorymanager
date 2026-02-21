package com.example.inventorymanager.controller;

import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.ItemRepositoryInterface;
import com.example.inventorymanager.view.InventoryView;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.mockito.Mockito.*;

import static java.util.Arrays.asList;

import org.junit.After;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class ItemControllerTest {

	@Mock
	private ItemRepositoryInterface itemRepository;

	@Mock
	private InventoryView inventoryView;

	private ItemController itemController;

	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		itemController = new ItemController(itemRepository, inventoryView);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testAllItems() {
		List<Item> items = asList(new Item());
		when(itemRepository.findAll())
			.thenReturn(items);
		itemController.getAllItems();
		verify(inventoryView)
			.displayItems(items);
	}

	@Test
	public void testGetAllItemsWithEmptyList() {
	    when(itemRepository.findAll()).thenReturn(null); // Mock an empty list

	    itemController.getAllItems();

	    verify(inventoryView).displayItems(null);
	}


	@Test
	public void testNewItemWhenItemDoesNotAlreadyExist() {
		Item item = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
		when(itemRepository.findById("1")).
			thenReturn(java.util.Optional.empty());
		itemController.addItem(item);
		InOrder inOrder = inOrder(itemRepository, inventoryView);
		inOrder.verify(itemRepository).save(item);
		inOrder.verify(inventoryView).addItem(item);
	}

	@Test
	public void testNewItemWhenItemAlreadyExists() {
		Item itemToAdd = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
		Item existingItem = new Item("1", "Laptop", 10, 999.99, "Gaming laptop");
		when(itemRepository.findById("1")).
			thenReturn(java.util.Optional.of(existingItem));
		itemController.addItem(itemToAdd);
		verify(inventoryView)
			.showErrorMessage("Already existing item with id 1", existingItem);
		verifyNoMoreInteractions(ignoreStubs(itemRepository));
	}


	@Test
	public void testUpdateItemWhenItemExists() {
	    Item itemToUpdate = new Item("1", "Laptop", 10, 999.99, "Updated gaming laptop");
	    when(itemRepository.findById("1"))
	        .thenReturn(java.util.Optional.of(itemToUpdate));

	    itemController.updateItem(itemToUpdate);

	    InOrder inOrder = inOrder(itemRepository, inventoryView);
	    inOrder.verify(itemRepository).update(itemToUpdate);
	    inOrder.verify(inventoryView).updateItem(itemToUpdate);
	}

	@Test
	public void testUpdateItemWhenItemDoesNotExist() {
	    Item itemToUpdate = new Item("1", "Laptop", 10, 999.99, "Updated gaming laptop");
	    when(itemRepository.findById("1"))
	        .thenReturn(java.util.Optional.empty());

	    itemController.updateItem(itemToUpdate);

	    verify(inventoryView)
	        .showErrorMessage("No existing item with id 1", itemToUpdate);
	    verifyNoMoreInteractions(ignoreStubs(itemRepository));
	}




	@Test
	public void testDeleteItemWhenItemExists() {
		Item itemToDelete = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
		when(itemRepository.findById("1")).
			thenReturn(java.util.Optional.of(itemToDelete));
		itemController.deleteItem(itemToDelete);
		InOrder inOrder = inOrder(itemRepository, inventoryView);
		inOrder.verify(itemRepository).delete("1");
		inOrder.verify(inventoryView).deleteItem(itemToDelete);
	}

	@Test
	public void testDeleteItemWhenItemDoesNotExist() {
		Item item = new Item("1", "Laptop", 10, 999.99, "High-end gaming laptop");
		when(itemRepository.findById("1")).
			thenReturn(java.util.Optional.empty());
		itemController.deleteItem(item);
		verify(inventoryView)
			.showErrorMessage("No existing item with id 1", item);
		verifyNoMoreInteractions(ignoreStubs(itemRepository));
	}


}
