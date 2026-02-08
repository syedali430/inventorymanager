package com.example.inventorymanager.app;

import com.example.inventorymanager.view.swing.InventoryFrame;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.awt.*;

public class InventoryApplicationTest {

    @Test
    public void testMainLaunchesFrameUsingConfigurablePort() {
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        int port = mongoServer.getLocalAddress().getPort();
        System.setProperty("inventory.mongo.host", "localhost");
        System.setProperty("inventory.mongo.port", String.valueOf(port));
        System.setProperty("inventory.test.skipInitialLoad", "true");

        try {
            InventoryApplication.main(new String[]{});
            long deadline = System.currentTimeMillis() + 2000; // 2s max wait
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
            System.clearProperty("inventory.mongo.host");
            System.clearProperty("inventory.mongo.port");
            System.clearProperty("inventory.test.skipInitialLoad");
            mongoServer.shutdownNow();
        }
    }

    @Test
    public void testApplyLookAndFeelReturnsFalseOnInvalidClass() {
        assertFalse(InventoryApplication.applyLookAndFeel("not.a.real.LookAndFeel"));
    }
}
