package com.example.inventorymanager.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ItemTest {

    @Test
    public void testDefaultConstructorAndSetters() {
        Item item = new Item();
        item.setId("id1");
        item.setName("name1");
        item.setQuantity(7);
        item.setPrice(3.5);
        item.setDescription("desc1");

        assertEquals("id1", item.getId());
        assertEquals("name1", item.getName());
        assertEquals(7, item.getQuantity());
        assertEquals(3.5, item.getPrice(), 0.0);
        assertEquals("desc1", item.getDescription());
        assertEquals("name1 | 7 | $3.5", item.toString());
    }
}

