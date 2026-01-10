package com.example.inventorymanager.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class ItemTest {

    @Test
    public void testConstructorAndGetters() {
        Item item = new Item("1", "Laptop", 10, 999.99, "Gaming");

        assertEquals("1", item.getId());
        assertEquals("Laptop", item.getName());
        assertEquals(10, item.getQuantity());
        assertEquals(999.99, item.getPrice(), 0.0);
        assertEquals("Gaming", item.getDescription());
    }

    @Test
    public void testSettersAndDefaultConstructor() {
        Item item = new Item();
        item.setId("2");
        item.setName("Tablet");
        item.setQuantity(3);
        item.setPrice(249.5);
        item.setDescription("Matte finish");

        assertEquals("2", item.getId());
        assertEquals("Tablet", item.getName());
        assertEquals(3, item.getQuantity());
        assertEquals(249.5, item.getPrice(), 0.0);
        assertEquals("Matte finish", item.getDescription());
    }

    @Test
    public void testToStringFormatsFields() {
        Item item = new Item("3", "Chair", 2, 19.5, "Ergonomic");

        assertEquals("Chair | 2 | $19.5", item.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        Item item1 = new Item("1", "Laptop", 10, 999.99, "Gaming");
        Item item2 = new Item("2", "Laptop", 10, 999.99, "Gaming");

        assertEquals(item1, item1);
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());

        assertNotEquals(item1, null);
        assertNotEquals(item1, "not an item");
        assertNotEquals(item1, new Item("1", "Laptop", 11, 999.99, "Gaming"));
        assertNotEquals(item1, new Item("1", "Laptop", 10, 199.99, "Gaming"));
        assertNotEquals(item1, new Item("1", "Desktop", 10, 999.99, "Gaming"));
        assertNotEquals(item1, new Item("1", "Laptop", 10, 999.99, "Other"));
    }

    @Test
    public void testEqualsWithNullFields() {
        Item item1 = new Item("1", null, 1, 1.0, null);
        Item item2 = new Item("2", null, 1, 1.0, null);

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1, new Item("2", "Name", 1, 1.0, null));
        assertNotEquals(item1, new Item("2", null, 1, 1.0, "Desc"));
    }
}
