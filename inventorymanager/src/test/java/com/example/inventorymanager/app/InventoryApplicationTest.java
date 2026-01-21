package com.example.inventorymanager.app;

import com.example.inventorymanager.view.swing.InventoryFrame;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

import java.awt.*;

public class InventoryApplicationTest {

    @Test
    public void testMainLaunchesFrameUsingConfigurablePort() {
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
            long deadline = System.currentTimeMillis() + 2000;
            InventoryFrame found = null;
            while (System.currentTimeMillis() < deadline && found == null) {
                for (Frame frame : Frame.getFrames()) {
                    if (frame instanceof InventoryFrame) {
                        found = (InventoryFrame) frame;
                        break;
                    }
                }
            }
            assertNotNull(found);
            if (found != null) {
                found.dispose();
            }
        } finally {
            mongoServer.shutdownNow();
        }
    }

    @Test
    public void testCreateFrameUsesDefaults() {
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        int port = mongoServer.getLocalAddress().getPort();
        System.setProperty("mongo.host", "localhost");
        System.setProperty("mongo.port", String.valueOf(port));
        try {
            InventoryApplication app = new InventoryApplication();
            InventoryFrame frame = app.createFrame();
            assertNotNull(frame);
            frame.dispose();
        } finally {
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
}
