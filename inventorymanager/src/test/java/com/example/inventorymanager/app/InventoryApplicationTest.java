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

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
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
