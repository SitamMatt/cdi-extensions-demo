package io.github.sitammatt.extensions;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.sitammatt.markers.Square;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;

public class CdiExtension implements Extension {
    <X> void ProcessInjectionTarget(@Observes ProcessInjectionTarget<X> pit){
        final InjectionTarget<X> it = pit.getInjectionTarget();
        final AnnotatedType<X> at = pit.getAnnotatedType();

        Logger.getAnonymousLogger()
            .log(Level.WARNING, "Processing injection target at class: " + at.getJavaClass().getName());

        InjectionTarget<X> wrapper = new InjectionTarget<X>() {
            @Override
            public X produce(CreationalContext<X> ctx){
                return it.produce(ctx);
            }

            @Override
            public void dispose(X instance){
                it.dispose(instance);
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints(){
                return it.getInjectionPoints();
            }

            @Override
            public void inject(X instance, CreationalContext<X> ctx){
                it.inject(instance, ctx);
                for(Field field : at.getJavaClass().getDeclaredFields()) {
                    // square value will be injected into fields annotated with 'Square'
                    Square annotation = field.getAnnotation(Square.class);
                    if(annotation != null){
                        int key = annotation.value();
                        field.setAccessible(true);
                        try {
                            field.set(instance, key * key);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            throw new RuntimeException("Could not resolve property", e);
                        }
                    }
                }
            }

            @Override
            public void postConstruct(X instance) {
                it.postConstruct(instance);
            }

            @Override
            public void preDestroy(X instance) {
                it.preDestroy(instance);
            }
        };

        pit.setInjectionTarget(wrapper);
    }
}
