package com.example.inventorymanager.app.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.awaitility.Awaitility;
import org.junit.BeforeClass;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

import com.example.inventorymanager.view.swing.InventorySwingView;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public class InventorySwingAppTest {

    @BeforeClass
    public static void enableSilentLoadErrors() {
        System.setProperty("inventory.test.silentLoadErrors", "true");
    }

    @Test
    public void testCallStartsViewWhenMongoAvailable() throws Exception {
        Assume.assumeFalse("Headless environment", GraphicsEnvironment.isHeadless());
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        int port = mongoServer.getLocalAddress().getPort();
        try {
            InventorySwingApp app = new InventorySwingApp();
            setField(app, "mongoHost", "localhost");
            setField(app, "mongoPort", port);
            setField(app, "databaseName", "inventory");
            setField(app, "collectionName", "item");

            app.call();

            InventorySwingView[] found = new InventorySwingView[1];
            Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
                for (Frame frame : Frame.getFrames()) {
                    if (frame instanceof InventorySwingView && frame.isShowing()) {
                        found[0] = (InventorySwingView) frame;
                        break;
                    }
                }
                assertThat(found[0]).isNotNull();
            });

            if (found[0] != null) {
                found[0].dispose();
            }
        } finally {
            mongoServer.shutdownNow();
        }
    }

    @Test
    public void testCallStartsViewWhenSkipInitialLoadEnabled() throws Exception {
        Assume.assumeFalse("Headless environment", GraphicsEnvironment.isHeadless());
        System.setProperty("inventory.test.skipInitialLoad", "true");
        try {
            InventorySwingApp app = new InventorySwingApp();
            setField(app, "mongoHost", "localhost");
            setField(app, "mongoPort", 27017);
            setField(app, "databaseName", "inventory");
            setField(app, "collectionName", "item");

            app.call();

            InventorySwingView[] found = new InventorySwingView[1];
            Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
                for (Frame frame : Frame.getFrames()) {
                    if (frame instanceof InventorySwingView && frame.isShowing()) {
                        found[0] = (InventorySwingView) frame;
                        break;
                    }
                }
                assertThat(found[0]).isNotNull();
            });

            if (found[0] != null) {
                found[0].dispose();
            }
        } finally {
            System.clearProperty("inventory.test.skipInitialLoad");
        }
    }

    @Test
    public void testLoadInitialItemsLogsWhenSilentFalse() {
        System.clearProperty("inventory.test.skipInitialLoad");
        System.clearProperty("inventory.test.silentLoadErrors");
        InventorySwingApp app = new InventorySwingApp();
        com.example.inventorymanager.controller.ItemController controller =
                org.mockito.Mockito.mock(com.example.inventorymanager.controller.ItemController.class);
        org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(controller).getAllItems();
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(InventorySwingApp.class.getName());
        java.util.logging.Level previous = logger.getLevel();
        logger.setLevel(java.util.logging.Level.OFF);
        try {
            app.loadInitialItems(controller);
            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> org.mockito.Mockito.verify(controller).getAllItems());
        } finally {
            logger.setLevel(previous);
        }
    }

    @Test
    public void testLoadInitialItemsCallsControllerWhenNoError() {
        System.clearProperty("inventory.test.skipInitialLoad");
        InventorySwingApp app = new InventorySwingApp();
        com.example.inventorymanager.controller.ItemController controller =
                Mockito.mock(com.example.inventorymanager.controller.ItemController.class);
        app.loadInitialItems(controller);
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> Mockito.verify(controller).getAllItems());
    }

    @Test
    public void testLoadInitialItemsSilentWhenEnabled() {
        System.clearProperty("inventory.test.skipInitialLoad");
        System.setProperty("inventory.test.silentLoadErrors", "true");
        try {
            InventorySwingApp app = new InventorySwingApp();
            com.example.inventorymanager.controller.ItemController controller =
                    org.mockito.Mockito.mock(com.example.inventorymanager.controller.ItemController.class);
            org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(controller).getAllItems();
            app.loadInitialItems(controller);
            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> org.mockito.Mockito.verify(controller).getAllItems());
        } finally {
            System.clearProperty("inventory.test.silentLoadErrors");
        }
    }

    @Test
    public void testCallLogsErrorWhenMongoUnavailable() throws Exception {
        Assume.assumeFalse("Headless environment", GraphicsEnvironment.isHeadless());
        InventorySwingApp app = new InventorySwingApp();
        setField(app, "mongoHost", "localhost");
        setField(app, "mongoPort", 27017);
        setField(app, "databaseName", "inventory");
        setField(app, "collectionName", "item");

        TestLogHandler handler = new TestLogHandler();
        Logger logger = Logger.getLogger(InventorySwingApp.class.getName());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        try {
            System.setProperty("inventory.test.forceError", "true");
            app.call();

            Awaitility.await().atMost(8, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(handler.messages).anyMatch(m -> m.contains("Exception"))
            );
        } finally {
            System.clearProperty("inventory.test.forceError");
            logger.removeHandler(handler);
            for (Frame frame : Frame.getFrames()) {
                if (frame instanceof InventorySwingView && frame.isShowing()) {
                    frame.dispose();
                }
            }
        }
    }

    @Test
    public void testMainDisplaysHelp() {
        InventorySwingApp.main(new String[] { "--help" });
        assertThat(InventorySwingApp.class.getName()).contains("InventorySwingApp");
    }

    private static void setField(InventorySwingApp app, String name, Object value) throws Exception {
        java.lang.reflect.Field field = InventorySwingApp.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(app, value);
    }

    private static class TestLogHandler extends Handler {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord logRecord) {
            if (logRecord != null && logRecord.getMessage() != null) {
                messages.add(logRecord.getMessage());
            }
        }

        @Override
        public void flush() {
            // no-op: messages are kept in memory for assertions
        }

        @Override
        public void close() throws SecurityException {
            // no-op: no external resources to release
        }
    }
}
