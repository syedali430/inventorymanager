package com.example.inventorymanager.learning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GuiceLearningTest {

    static class MyService {
    }

    static class MyClient {
        final MyService service;

        @Inject
        MyClient(MyService service) {
            this.service = service;
        }
    }

    interface IMyService {
    }

    static class MyConcreteService implements IMyService {
    }

    static class MyGenericClient {
        final IMyService service;

        @Inject
        MyGenericClient(IMyService service) {
            this.service = service;
        }
    }

    @Test
    public void canInstantiateConcreteClassesWithoutConfiguration() {
        Module module = new AbstractModule() {
        };
        Injector injector = Guice.createInjector(module);
        MyClient client = injector.getInstance(MyClient.class);
        assertNotNull(client.service);
    }

    @Test
    public void injectAbstractType() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(IMyService.class).to(MyConcreteService.class);
            }
        };
        Injector injector = Guice.createInjector(module);
        MyGenericClient client = injector.getInstance(MyGenericClient.class);
        assertNotNull(client.service);
        assertEquals(MyConcreteService.class, client.service.getClass());
    }
}
