package com.example.inventorymanager.learning;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GuiceFieldAndMethodInjectionLearningTest {

    interface IMyService {
    }

    static class MyService implements IMyService {
    }

    static class MyClientWithInjectedField {
        @Inject
        IMyService service;
    }

    static class MyClientWithInjectedMethod {
        IMyService service;

        @Inject
        public void init(IMyService service) {
            this.service = service;
        }
    }

    static class MyClientWithOptionalInjection {
        @Inject(optional = true)
        IMyService service;
    }

    @Test
    public void fieldAndMethodInjection() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(IMyService.class).to(MyService.class);
            }
        };
        Injector injector = Guice.createInjector(module);
        MyClientWithInjectedField client1 = injector.getInstance(MyClientWithInjectedField.class);
        MyClientWithInjectedMethod client2 = injector.getInstance(MyClientWithInjectedMethod.class);
        assertNotNull(client1.service);
        assertNotNull(client2.service);
    }

    @Test
    public void injectMembers() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(IMyService.class).to(MyService.class);
            }
        };
        Injector injector = Guice.createInjector(module);
        MyClientWithInjectedField client1 = new MyClientWithInjectedField();
        MyClientWithInjectedMethod client2 = new MyClientWithInjectedMethod();
        injector.injectMembers(client1);
        injector.injectMembers(client2);
        assertNotNull(client1.service);
        assertNotNull(client2.service);
    }

    @Test
    public void optionalInjectionIsIgnoredWhenMissing() {
        Injector injector = Guice.createInjector();
        MyClientWithOptionalInjection client = injector.getInstance(MyClientWithOptionalInjection.class);
        assertNull(client.service);
    }
}