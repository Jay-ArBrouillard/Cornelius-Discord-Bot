package rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/")
public class REST {

    @GET
    @Produces("text/plain")
    public String getClichedMessage() {
        return "Hello, World!";
    }
}