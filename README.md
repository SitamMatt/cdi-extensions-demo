# CDI Extensions Samples

### Qualifiers

Create annotation working as qulifier

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

###