package com.example.inventorymanager.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class ItemTest {

    @Test
    public void testToStringFormatsFields() {
        Item item = new Item("3", "Chair", 2, 19.5, "Ergonomic");

        assertEquals("Chair | 2 | $19.5", item.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        Item item1 = new Item();
        item1.setId("1");
        item1.setName("Laptop");
        item1.setQuantity(10);
        item1.setPrice(999.99);
        item1.setDescription("Gaming");
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
