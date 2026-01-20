package com.example.inventorymanager.app;

import com.example.inventorymanager.controller.ItemController;
import com.example.inventorymanager.repository.ItemRepository;
import com.example.inventorymanager.repository.ItemRepositoryInterface;
import com.example.inventorymanager.view.swing.InventoryFrame;

import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class InventoryApplication implements Callable<Integer> {

    @Option(names = { "--mongo-host" }, description = "MongoDB host address")
    private String mongoHost = "localhost";

    @Option(names = { "--mongo-port" }, description = "MongoDB host port")
    private int mongoPort = 27017;

    @Option(names = { "--db-name" }, description = "MongoDB database name")
    private String databaseName = "inventorydb";

    @Option(names = { "--db-collection" }, description = "MongoDB collection name")
    private String collectionName = "items";

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
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Integer call() {
        applyLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(
            () -> createFrame(mongoHost, mongoPort, databaseName, collectionName).setVisible(true)
        );
        return 0;
    }

    public static void main(String[] args) {
        new CommandLine(new InventoryApplication()).execute(args);
    }
}
