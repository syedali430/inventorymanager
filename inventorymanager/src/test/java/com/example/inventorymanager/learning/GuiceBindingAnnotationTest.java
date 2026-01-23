package com.example.inventorymanager.learning;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class GuiceBindingAnnotationTest {

    static class MyFileWrapper {
        File file;

        @Inject
        public MyFileWrapper(@Named("PATH") String path, @Named("NAME") String name) {
            file = new File(path, name);
        }
    }

    @BindingAnnotation
    @Target({ FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME)
    private @interface FilePath {
    }

    @BindingAnnotation
    @Target({ FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME)
    private @interface FileName {
    }

    static class MyFileWrapper2 {
        File file;

        @Inject
        public MyFileWrapper2(@FilePath String path, @FileName String name) {
            file = new File(path, name);
        }
    }

    @Test
    public void bindingAnnotationsWithNamed() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class)
                    .annotatedWith(Names.named("PATH"))
                    .toInstance("src/test/resources");
                bind(String.class)
                    .annotatedWith(Names.named("NAME"))
                    .toInstance("afile.txt");
            }
        };
        Injector injector = Guice.createInjector(module);
        MyFileWrapper fileWrapper = injector.getInstance(MyFileWrapper.class);
        assertTrue(fileWrapper.file.exists());
    }

    @Test
    public void customBindingAnnotations() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class)
                    .annotatedWith(FilePath.class)
                    .toInstance("src/test/resources");
                bind(String.class)
                    .annotatedWith(FileName.class)
                    .toInstance("afile.txt");
            }
        };
        Injector injector = Guice.createInjector(module);
        MyFileWrapper2 fileWrapper = injector.getInstance(MyFileWrapper2.class);
        assertTrue(fileWrapper.file.exists());
    }
}