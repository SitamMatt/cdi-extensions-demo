package io.github.sitammatt.services;

import io.github.sitammatt.markers.HelloDependency;

@HelloDependency
public class Service {
    
    public String getMessage() {
        return "Hello from service";
    }
}
