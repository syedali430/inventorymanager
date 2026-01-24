package com.example.inventorymanager.app;

import com.example.inventorymanager.controller.ItemController;
import com.example.inventorymanager.guice.InventorySwingMongoModule;
import com.example.inventorymanager.repository.ItemRepository;
import com.example.inventorymanager.repository.ItemRepositoryInterface;
import com.example.inventorymanager.view.swing.InventoryFrame;

import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.google.inject.Guice;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class InventoryApplication implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryApplication.class);

    @Option(names = { "--mongo-host" }, description = "MongoDB host address")
    private String mongoHost = "localhost";

    @Option(names = { "--mongo-port" }, description = "MongoDB host port")
    private int mongoPort = 27017;

    @Option(names = { "--db-name" }, description = "MongoDB database name")
    private String databaseName = "inventorydb";

    @Option(names = { "--db-collection" }, description = "MongoDB collection name")
    private String collectionName = "items";

    @Option(names = { "--headless" }, description = "Run without UI and exit after checking MongoDB connectivity")
    private boolean headless;

    public InventoryFrame createFrame() {
        ItemRepositoryInterface repository = ItemRepository.createDefault();
        InventoryFrame frame = new InventoryFrame();
        ItemController controller = new ItemController(repository, frame);
        frame.setController(controller);
        controller.getAllItems();
        return frame;
    }

    public InventoryFrame createFrame(String host, int port, String dbName, String collection) {
        ItemRepositoryInterface repository = ItemRepository.create(host, port, dbName, collection);
        InventoryFrame frame = new InventoryFrame();
        ItemController controller = new ItemController(repository, frame);
        frame.setController(controller);
        controller.getAllItems();
        return frame;
    }

    boolean applyLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            return true;
        } catch (Exception e) {
            LOGGER.warn("Failed to apply look and feel {}", className, e);
            return false;
        }
    }

    @Override
    public Integer call() {
        if (headless) {
            boolean connected = waitForMongo(mongoHost, mongoPort, databaseName, 10, 500);
            if (connected) {
                LOGGER.info("Connected to MongoDB at {}:{}", mongoHost, mongoPort);
                return 0;
            }
            LOGGER.error("Unable to connect to MongoDB at {}:{}", mongoHost, mongoPort);
            return 1;
        }
        applyLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> {
            InventoryFrame frame = createFrameWithGuice(mongoHost, mongoPort, databaseName, collectionName);
            frame.start();
        });
        return 0;
    }

    public static void main(String[] args) {
        new CommandLine(new InventoryApplication()).execute(args);
    }

    private InventoryFrame createFrameWithGuice(String host, int port, String dbName, String collection) {
        return Guice.createInjector(
            new InventorySwingMongoModule()
                .mongoHost(host)
                .mongoPort(port)
                .databaseName(dbName)
                .collectionName(collection)
        ).getInstance(InventoryFrame.class);
    }

    private boolean waitForMongo(String host, int port, String dbName, int attempts, long delayMillis) {
        for (int i = 0; i < attempts; i++) {
            if (canConnect(host, port, dbName)) {
                return true;
            }
            try {
                sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    protected void sleep(long delayMillis) throws InterruptedException {
        Thread.sleep(delayMillis);
    }

    private boolean canConnect(String host, int port, String dbName) {
        try (MongoClient client = new MongoClient(host, port)) {
            client.getDatabase(dbName).listCollectionNames().first();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
