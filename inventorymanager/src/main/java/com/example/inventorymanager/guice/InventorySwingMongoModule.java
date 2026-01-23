package com.example.inventorymanager.guice;

import com.example.inventorymanager.controller.ItemController;
import com.example.inventorymanager.controller.ItemControllerInterface;
import com.example.inventorymanager.repository.ItemRepository;
import com.example.inventorymanager.repository.ItemRepositoryInterface;
import com.example.inventorymanager.view.swing.InventoryFrame;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.mongodb.MongoClient;

public class InventorySwingMongoModule extends AbstractModule {

    private String mongoHost = "localhost";
    private int mongoPort = 27017;
    private String databaseName = "inventorydb";
    private String collectionName = "items";

    public InventorySwingMongoModule mongoHost(String mongoHost) {
        this.mongoHost = mongoHost;
        return this;
    }

    public InventorySwingMongoModule mongoPort(int mongoPort) {
        this.mongoPort = mongoPort;
        return this;
    }

    public InventorySwingMongoModule databaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public InventorySwingMongoModule collectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(MongoHost.class).toInstance(mongoHost);
        bind(Integer.class).annotatedWith(MongoPort.class).toInstance(mongoPort);
        bind(String.class).annotatedWith(MongoDbName.class).toInstance(databaseName);
        bind(String.class).annotatedWith(MongoCollectionName.class).toInstance(collectionName);

        bind(ItemRepositoryInterface.class).to(ItemRepository.class);

        install(new FactoryModuleBuilder()
            .implement(ItemControllerInterface.class, ItemController.class)
            .build(ItemControllerFactory.class));
    }

    @Provides
    @Singleton
    MongoClient mongoClient(@MongoHost String host, @MongoPort int port) {
        return new MongoClient(host, port);
    }

    @Provides
    InventoryFrame inventoryFrame(ItemControllerFactory controllerFactory) {
        InventoryFrame frame = new InventoryFrame();
        frame.setController(controllerFactory.create(frame));
        return frame;
    }
}