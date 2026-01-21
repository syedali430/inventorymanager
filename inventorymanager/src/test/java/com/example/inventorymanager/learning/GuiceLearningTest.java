package com.example.inventorymanager.learning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class GuiceLearningTest {

    interface Greeter {
        String message();
    }

    static class HelloGreeter implements Greeter {
        @Override
        public String message() {
            return "hello";
        }
    }

    static class Client {
        final Greeter greeter;
        final String prefix;

        @Inject
        Client(Greeter greeter, @Named("prefix") String prefix) {
            this.greeter = greeter;
            this.prefix = prefix;
        }

        String greet() {
            return prefix + greeter.message();
        }
    }

    static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Greeter.class).to(HelloGreeter.class);
            bind(String.class).annotatedWith(Names.named("prefix")).toInstance(">>");
        }
    }

    @Test
    public void testInjectorProvidesDependencies() {
        Injector injector = Guice.createInjector(new TestModule());
        Client client = injector.getInstance(Client.class);
        assertNotNull(client);
        assertEquals(">>hello", client.greet());
    }
}
