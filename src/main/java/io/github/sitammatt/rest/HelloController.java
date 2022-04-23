package io.github.sitammatt.rest;

import io.github.sitammatt.markers.HelloDependency;
import io.github.sitammatt.markers.Square;
import io.github.sitammatt.services.Service;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
@RequestScoped
public class HelloController {
    
    @Inject
    @HelloDependency /* without this annotation application will throw unsatisified dependency exception */
    private Service service; 

    @Square(2)
    private int squareValue;

    @GET
    @Path("hello")
    public Response hello() {
        return Response.ok("Hello World").build();
    }

    @GET
    @Path("service")
    public Response helloService() {
        return Response.ok(service.getMessage()).build();
    }

    @GET
    @Path("square")
    public Response helloSquare() {
        return Response.ok(squareValue).build();
    }
}
