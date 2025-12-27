package com.example.inventorymanager.app;

import com.example.inventorymanager.view.swing.InventoryFrame;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertTrue;

public class InventoryApplicationTest {

    @Test
    public void testMainLaunchesFrameUsingConfigurablePort() throws Exception {
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        int port = mongoServer.getLocalAddress().getPort();
        System.setProperty("inventory.mongo.host", "localhost");
        System.setProperty("inventory.mongo.port", String.valueOf(port));

        try {
            InventoryApplication.main(new String[]{});
            boolean frameFound = false;
            for (int i = 0; i < 6 && !frameFound; i++) {
                Thread.sleep(300);
                for (Frame frame : Frame.getFrames()) {
                    if (frame instanceof InventoryFrame) {
                        frameFound = true;
                        frame.dispose();
                    }
                }
            }
            assertTrue(frameFound);
        } finally {
            System.clearProperty("inventory.mongo.host");
            System.clearProperty("inventory.mongo.port");
            mongoServer.shutdownNow();
        }
    }
}
