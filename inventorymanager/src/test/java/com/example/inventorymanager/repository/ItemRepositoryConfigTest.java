package com.example.inventorymanager.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ItemRepositoryConfigTest {

    @Test
    public void testDefaultPortFallsBackOnInvalidValue() {
        System.setProperty("mongo.port", "not-a-number");
        try {
            int fallbackPort = ItemRepository.defaultPort();
            assertEquals(27017, fallbackPort);
        } finally {
            System.clearProperty("mongo.port");
        }
    }

    @Test
    public void testDefaultPortUsesInventoryPropertyWhenMongoPortMissing() {
        System.clearProperty("mongo.port");
        System.setProperty("inventory.mongo.port", "27019");
        try {
            int port = ItemRepository.defaultPort();
            assertEquals(27019, port);
        } finally {
            System.clearProperty("inventory.mongo.port");
        }
    }

    @Test
    public void testDefaultHostUsesSystemProperty() {
        System.setProperty("mongo.host", "customhost");
        try {
            String host = ItemRepository.defaultHost();
            assertEquals("customhost", host);
        } finally {
            System.clearProperty("mongo.host");
        }
    }

    @Test
    public void testDefaultHostUsesInventoryPropertyWhenMongoHostMissing() {
        System.clearProperty("mongo.host");
        System.setProperty("inventory.mongo.host", "backuphost");
        try {
            String host = ItemRepository.defaultHost();
            assertEquals("backuphost", host);
        } finally {
            System.clearProperty("inventory.mongo.host");
        }
    }
}
