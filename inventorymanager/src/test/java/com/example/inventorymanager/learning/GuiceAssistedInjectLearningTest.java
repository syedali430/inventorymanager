package com.example.inventorymanager.learning;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class GuiceAssistedInjectLearningTest {

    interface IMyView {
    }

    static class MyView implements IMyView {
    }

    interface IMyRepository {
    }

    static class MyRepository implements IMyRepository {
    }

    interface IMyController {
    }

    static class MyController implements IMyController {
        IMyView view;
        IMyRepository repository;

        @Inject
        public MyController(@Assisted IMyView view, IMyRepository repository) {
            this.view = view;
            this.repository = repository;
        }
    }

    interface MyControllerFactory {
        IMyController create(IMyView view);
    }

    @Test
    public void assistedInject() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(IMyRepository.class).to(MyRepository.class);
                install(new FactoryModuleBuilder()
                    .implement(IMyController.class, MyController.class)
                    .build(MyControllerFactory.class));
            }
        };
        Injector injector = Guice.createInjector(module);
        MyControllerFactory controllerFactory = injector.getInstance(MyControllerFactory.class);
        MyController controller = (MyController) controllerFactory.create(new MyView());
        assertNotNull(controller.view);
        assertNotNull(controller.repository);
    }
}