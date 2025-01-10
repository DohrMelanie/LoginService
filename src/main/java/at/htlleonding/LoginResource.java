package at.htlleonding;

import at.htlleonding.jwt.JWTRequired;
import at.htlleonding.jwt.JWTService;
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
    public Response register(UserDto user) {
        log.info("register + hash and salt pw");
        try {
            loginService.addUser(new User(user.getUsername(), user.getPassword(), user.getTelephoneNumber()));
        } catch (IllegalArgumentException e) {
            return Response.status(400, e.getMessage()).build();
        }
        return Response.status(201).build();
    }

    @GET
    @Path("/login")
    public Response login(@QueryParam("username") String username, @QueryParam("password") String password) {
        log.info("login");
        log.info("password: {}", password);
        try {
            if (loginService.checkPassword(username, password)) {
                String token = JWTService.generateToken(username, 30);
                return Response.ok().header("Authorization", "Bearer " + token).build();
            } else {
                return Response.status(400).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(400).build();
        } catch (Exception e) {
            return Response.status(401).build();
        }
    }

    @GET
    @Path("/resetpw")
    @JWTRequired //this enforces that the user sends a jwt
    public Response resetPassword(@QueryParam("username") String username) {
        log.info("reset password");
        loginService.resetPassword(username);
        return Response.status(200).build();
    }
}
