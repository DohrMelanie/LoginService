package at.htlleonding;

import at.htlleonding.dtos.LoginDto;
import at.htlleonding.dtos.RegisterDto;
import at.htlleonding.dtos.ResetPasswordDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
        try {
            RegisterDto user = new RegisterDto("testaege@gmail.com", "password12354", "+123456789");

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(user)
                    .when()
                    .post("/api/v1/register")
                    .then()
                    .statusCode(201);
        } finally {
            loginService.deleteUserByName(testUser.getUsername());
        }
    }

    @Test
    void testLoginSuccess() {
        try {
            loginService.addUser(testUser);

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(new LoginDto(testUser.getUsername(), "password123"))
                    .when()
                    .post("/api/v1/login")
                    .then()
                    .statusCode(200)
                    .header("Authorization", startsWith("Bearer"));
        } finally {
            loginService.deleteUserByName(testUser.getUsername());
        }
    }

    @Test
    void testLoginFailureInvalidPassword() {
        try {
            loginService.addUser(testUser);
            
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(new LoginDto(testUser.getUsername(), "wrongpassword"))
                    .when()
                    .post("/api/v1/login")
                    .then()
                    .statusCode(400);
        } finally {
            loginService.deleteUserByName(testUser.getUsername());
        }
    }

    @Test
    void testResetPassword() {
        try {
            loginService.addUser(testUser);

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .pathParam("username", testUser.getUsername())
                    .when()
                    .post("/api/v1/resetpw/{username}")
                    .then()
                    .statusCode(200)
                    .body(is(not(emptyString())));
        } finally {
            loginService.deleteUserByName(testUser.getUsername());
        }
    }

    @Test
    void testResetPasswordWithCode() {
        try {
            loginService.addUser(testUser);
            String resetCode = loginService.resetPassword(testUser.getUsername());

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(new ResetPasswordDto(testUser.getUsername(), resetCode, "newPassword"))
                    .when()
                    .post("/api/v1/resetpw/code/")
                    .then()
                    .statusCode(200);
        } finally {
            loginService.deleteUserByName(testUser.getUsername());
        }
    }
}
