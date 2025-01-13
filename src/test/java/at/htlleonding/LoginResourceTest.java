package at.htlleonding;

import at.htlleonding.dtos.LoginDto;
import at.htlleonding.dtos.RegisterDto;
import at.htlleonding.dtos.ResetPasswordDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@QuarkusTest
public class LoginResourceTest {
    static User testUser;

    @Inject
    LoginService loginService;

    @AfterAll
    public static void tearDown() {
        RestAssured.reset();
    }

    @BeforeAll
    public static void setUp() {
        testUser = new User("test@gmail.com", "password123", "+123456789");
    }

    @Test
    void testRegisterSuccess() {
        RegisterDto user = new RegisterDto("testaege@gmail.com", "password12354", "+123456789");
        
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/api/v1/register")
                .then()
                .statusCode(201);
        
        loginService.deleteUserByName(user.getUsername());
    }

    @Test
    void testLoginSuccess() {
        loginService.addUser(testUser);

        RestAssured.given()
                .body(new LoginDto(testUser.getUsername(), "password123"))
                .when()
                .get("/api/v1/login")
                .then()
                .statusCode(200)
                .header("Authorization", startsWith("Bearer"));
        
        loginService.deleteUserByName(testUser.getUsername());
    }

    @Test
    void testLoginFailureInvalidPassword() {
        RestAssured.given()
                .body(new LoginDto(testUser.getUsername(), "wrongpassword"))
                .when()
                .get("/api/v1/login")
                .then()
                .statusCode(400);
    }

    @Test
    void testResetPassword() {
        loginService.addUser(testUser);

        RestAssured.given()
                .pathParam("username", testUser.getUsername())
                .when()
                .get("/api/v1/resetpw/{username}")
                .then()
                .statusCode(200)
                .body(is(not(emptyString())));

        loginService.deleteUserByName(testUser.getUsername());
    }

    @Test
    void testResetPasswordWithCode() {
        loginService.addUser(testUser);
        String resetCode = loginService.resetPassword(testUser.getUsername());

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new ResetPasswordDto(testUser.getUsername(), resetCode, "newPassword"))
                .when()
                .get("/api/v1/resetpw/code/")
                .then()
                .statusCode(200);

        loginService.deleteUserByName(testUser.getUsername());
    }
}
