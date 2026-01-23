package com.example.inventorymanager.learning;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;

public class GuiceSingletonLearningTest {

    interface IMyService {
    }

    static class MyService implements IMyService {
    }

    static class MyClient {
        final IMyService service;

        @Inject
        MyClient(IMyService service) {
            this.service = service;
        }
    }

    @Test
    public void bindToSingleton() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(IMyService.class).to(MyService.class).in(Singleton.class);
            }
        };
        Injector injector = Guice.createInjector(module);
        MyClient client1 = injector.getInstance(MyClient.class);
        MyClient client2 = injector.getInstance(MyClient.class);
        assertSame(client1.service, client2.service);
    }

    @Test
    public void singletonPerInjector() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(IMyService.class).to(MyService.class).in(Singleton.class);
            }
        };
        MyClient client1 = Guice.createInjector(module).getInstance(MyClient.class);
        MyClient client2 = Guice.createInjector(module).getInstance(MyClient.class);
        assertNotSame(client1.service, client2.service);
    }
}