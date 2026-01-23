package com.example.inventorymanager.learning;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class GuiceCyclicDependenciesLearningTest {

    interface IMyView {
    }

    static class MyView implements IMyView {
        IMyController controller;

        public void setController(IMyController controller) {
            this.controller = controller;
        }
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

    static class MyViewProvider implements Provider<MyView> {
        @Inject
        private MyControllerFactory controllerFactory;

        @Override
        public MyView get() {
            MyView view = new MyView();
            view.setController(controllerFactory.create(view));
            return view;
        }
    }

    @Test
    public void cyclicDependencies() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(IMyRepository.class).to(MyRepository.class);
                bind(MyView.class).toProvider(MyViewProvider.class);
                install(new FactoryModuleBuilder()
                    .implement(IMyController.class, MyController.class)
                    .build(MyControllerFactory.class));
            }
        };
        Injector injector = Guice.createInjector(module);
        MyView view = injector.getInstance(MyView.class);
        assertSame(view, ((MyController) view.controller).view);
        assertNotNull(((MyController) view.controller).repository);
    }
}