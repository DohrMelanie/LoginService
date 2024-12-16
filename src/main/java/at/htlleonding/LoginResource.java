package at.htlleonding;

import io.quarkus.security.Authenticated;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Consumes("application/json")
@Path("/api/v1")
@Produces("application/json")
@Slf4j
public class LoginResource {
    private final LoginService loginService;
    public LoginResource(@NotNull final LoginService loginService) {
        log.info("start");
        this.loginService = loginService;
    }

    @POST
    @Path("/register")
    public Response register(User user) {
        // TODO: jwt ?
        log.info("register + hash and salt pw");
        loginService.addUser(user);
        return Response.status(201).build();
    }

    @GET
    @Path("/login")
    @Authenticated // NOTWENDIG für jwt
    public Response login(@QueryParam("username") String username, @QueryParam("password") String password) {
        log.info("login");
        // TODO: jwt
        return loginService.checkPassword(username, password) ? Response.status(200).build() : Response.status(401).build();
    }
}
