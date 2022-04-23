# CDI Extensions Samples

### Qualifiers

Create annotation working as qualifier

```java
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ METHOD, FIELD, PARAMETER, TYPE })
public @interface HelloDependency {

}
```

Annotate dependency with qualifier

```java
@HelloDependency
public class Service {
    
    public String getMessage() {
        return "Hello from service";
    }
}
```

Add qualifier to injection point

```java
@RequestScoped
public class HelloController {
    
    @Inject
    @HelloDependency
    private Service service; 
}
```

### Injection targets

Declare annotation class which will mark fields where custom value will be injected

```java
import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface Square {
    int value();
}
```

Declare **Extension** class which will process the injection targets

```java
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
                        } catch (IllegalArgumentException 
                                | IllegalAccessException e) {
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
```

Add fully qualified class name that implements Extension interface to file located at *src\main\resources\META-INF\services\jakarta.enterprise.inject.spi.Extension*

```
io.github.sitammatt.extensions.CdiExtension
```

Example usage of square injection

```java
@RequestScoped
public class HelloController {
    
    @Square(2) // square of 2 will be injected
    private int squareValue;
}
```