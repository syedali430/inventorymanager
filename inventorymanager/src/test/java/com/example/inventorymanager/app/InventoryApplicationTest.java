package com.example.inventorymanager.app;

import com.example.inventorymanager.view.swing.InventoryFrame;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.awaitility.Awaitility;
import org.assertj.swing.edt.GuiActionRunner;
import org.junit.Assume;
import org.junit.Test;
import picocli.CommandLine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class InventoryApplicationTest {

    @Test
    public void testMainLaunchesFrameUsingConfigurablePort() {
        assumeGraphicsAvailable();
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        int port = mongoServer.getLocalAddress().getPort();

        try {
            InventoryApplication.main(new String[]{
                    "--mongo-host=localhost",
                    "--mongo-port=" + port,
                    "--db-name=inventorydb",
                    "--db-collection=items"
            });
            InventoryFrame found = awaitInventoryFrame();
            assertNotNull(found);
        } finally {
            disposeInventoryFrames();
            mongoServer.shutdownNow();
        }
    }

    @Test
    public void testCreateFrameUsesDefaults() {
        assumeGraphicsAvailable();
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        int port = mongoServer.getLocalAddress().getPort();
        System.setProperty("mongo.host", "localhost");
        System.setProperty("mongo.port", String.valueOf(port));
        try {
            InventoryApplication app = new InventoryApplication();
            InventoryFrame frame = GuiActionRunner.execute(
                    (java.util.concurrent.Callable<InventoryFrame>) app::createFrame
            );
            assertNotNull(frame);
            frame.dispose();
        } finally {
            disposeInventoryFrames();
            System.clearProperty("mongo.host");
            System.clearProperty("mongo.port");
            mongoServer.shutdownNow();
        }
    }

    @Test
    public void testApplyLookAndFeelReturnsFalseOnInvalidClass() {
        InventoryApplication app = new InventoryApplication();
        boolean applied = app.applyLookAndFeel("not.a.real.LookAndFeel");
        assertNotNull(app);
        org.junit.Assert.assertFalse(applied);
    }

    @Test
    public void testApplyLookAndFeelReturnsTrueOnValidClass() {
        InventoryApplication app = new InventoryApplication();
        boolean applied = app.applyLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
        assertTrue(applied);
    }

    @Test
    public void testCreateFrameWithExplicitValues() {
        assumeGraphicsAvailable();
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        int port = mongoServer.getLocalAddress().getPort();
        try {
            InventoryApplication app = new InventoryApplication();
            InventoryFrame frame = GuiActionRunner.execute(
                    (java.util.concurrent.Callable<InventoryFrame>) () -> app.createFrame("localhost", port,
                            "inventorydb", "items"));
            assertNotNull(frame);
            frame.dispose();
        } finally {
            disposeInventoryFrames();
            mongoServer.shutdownNow();
        }
    }

    @Test
    public void testHeadlessReturnsZeroWhenMongoAvailable() {
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        int port = mongoServer.getLocalAddress().getPort();
        try {
            int exitCode = new CommandLine(new InventoryApplication()).execute(
                    "--headless",
                    "--mongo-host=localhost",
                    "--mongo-port=" + port,
                    "--db-name=inventorydb"
            );
            assertEquals(0, exitCode);
        } finally {
            mongoServer.shutdownNow();
        }
    }

    @Test
    public void testHeadlessReturnsOneWhenMongoUnavailable() {
        int port = findFreePort();
        int exitCode = new CommandLine(new InventoryApplication()).execute(
                "--headless",
                "--mongo-host=localhost",
                "--mongo-port=" + port,
                "--db-name=inventorydb"
        );
        assertEquals(1, exitCode);
    }

    @Test
    public void testCallNonHeadlessReturnsZero() throws Exception {
        assumeGraphicsAvailable();
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        int port = mongoServer.getLocalAddress().getPort();
        InventoryApplication app = new InventoryApplication();

        java.lang.reflect.Field hostField = InventoryApplication.class.getDeclaredField("mongoHost");
        java.lang.reflect.Field portField = InventoryApplication.class.getDeclaredField("mongoPort");
        java.lang.reflect.Field dbField = InventoryApplication.class.getDeclaredField("databaseName");
        java.lang.reflect.Field collectionField = InventoryApplication.class.getDeclaredField("collectionName");
        hostField.setAccessible(true);
        portField.setAccessible(true);
        dbField.setAccessible(true);
        collectionField.setAccessible(true);
        hostField.set(app, "localhost");
        portField.set(app, port);
        dbField.set(app, "inventorydb");
        collectionField.set(app, "items");

        try {
            int result = app.call();
            assertEquals(0, result);
            InventoryFrame frame = awaitInventoryFrame();
            assertNotNull(frame);
        } finally {
            disposeInventoryFrames();
            mongoServer.shutdownNow();
        }
    }

    @Test
    public void testWaitForMongoReturnsFalseWhenInterrupted() throws Exception {
        InventoryApplication app = new InventoryApplication();
        Method method = InventoryApplication.class.getDeclaredMethod("waitForMongo",
                String.class, int.class, String.class, int.class, long.class);
        method.setAccessible(true);
        Thread.currentThread().interrupt();
        try {
            boolean result = (boolean) method.invoke(app, "localhost", -1, "inventorydb", 1, 1L);
            assertFalse(result);
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    public void testCanConnectReturnsTrueAndFalse() throws Exception {
        InventoryApplication app = new InventoryApplication();
        Method method = InventoryApplication.class.getDeclaredMethod("canConnect",
                String.class, int.class, String.class);
        method.setAccessible(true);

        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        int port = mongoServer.getLocalAddress().getPort();
        try {
            boolean canConnect = (boolean) method.invoke(app, "localhost", port, "inventorydb");
            assertTrue(canConnect);
        } finally {
            mongoServer.shutdownNow();
        }

        boolean cannotConnect = (boolean) method.invoke(app, "localhost", -1, "inventorydb");
        assertFalse(cannotConnect);
    }

    private static void assumeGraphicsAvailable() {
        Assume.assumeFalse("Headless environment", GraphicsEnvironment.isHeadless());
    }

    private static InventoryFrame awaitInventoryFrame() {
        AtomicReference<InventoryFrame> ref = new AtomicReference<>();
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            InventoryFrame frame = findInventoryFrame();
            assertNotNull(frame);
            ref.set(frame);
        });
        return ref.get();
    }

    private static InventoryFrame findInventoryFrame() {
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof InventoryFrame) {
                return (InventoryFrame) frame;
            }
        }
        return null;
    }

    private static void disposeInventoryFrames() {
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof InventoryFrame) {
                frame.dispose();
            }
        }
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to allocate a free port", e);
        }
    }
}
