package com.example.inventorymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public class AppTest {

    @Test
    public void testMainDoesNotWriteToStdout() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            App.main(new String[]{});
        } finally {
            System.setOut(originalOut);
        }
        assertEquals("", captured.toString());
    }

    @Test
    public void testMainHeadlessRunsWithMongo() {
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind("localhost", 0);
        int port = mongoServer.getLocalAddress().getPort();
        try {
            App app = new App();
            assertNotNull(app);
            App.main(new String[] {
                    "--headless",
                    "--mongo-host=localhost",
                    "--mongo-port=" + port,
                    "--db-name=inventorydb"
            });
        } finally {
            mongoServer.shutdownNow();
        }
    }
}
