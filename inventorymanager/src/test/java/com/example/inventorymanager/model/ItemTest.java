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

    @Test
    public void testHashCodeMatchesExpectedForNonNullFields() {
        Item item = new Item("1", "Laptop", 10, 999.99, "Gaming");

        assertEquals(expectedHash("Laptop", 10, 999.99, "Gaming"), item.hashCode());
    }

    @Test
    public void testHashCodeMatchesExpectedForNullFields() {
        Item item = new Item("1", null, 1, 1.0, null);

        assertEquals(expectedHash(null, 1, 1.0, null), item.hashCode());
    }

    private static int expectedHash(String name, int quantity, double price, String description) {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + quantity;
        long temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
