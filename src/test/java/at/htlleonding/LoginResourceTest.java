package at.htlleonding;

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
        UserDto user = new UserDto("testaege@gmail.com", "password12354", "+123456789");
        
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

        String username = testUser.getUsername();
        String password = "password123";

        RestAssured.given()
                .queryParam("username", username)
                .queryParam("password", password)
                .when()
                .get("/api/v1/login")
                .then()
                .statusCode(200)
                .header("Authorization", startsWith("Bearer"));
        
        loginService.deleteUserByName(testUser.getUsername());
    }

    @Test
    void testLoginFailureInvalidPassword() {
        String username = testUser.getUsername();
        String password = "wrongpassword";

        RestAssured.given()
                .queryParam("username", username)
                .queryParam("password", password)
                .when()
                .get("/api/v1/login")
                .then()
                .statusCode(400);
    }

    @Test
    void testResetPasswordUnauthorized() {
        RestAssured.given()
                .queryParam("username", "test@gmail.com")
                .when()
                .get("/api/v1/resetpw")
                .then()
                .statusCode(401);
    }

    @Test
    void testResetPasswordWithInvalidToken() {
        String invalidToken = "invalid.jwt.token";
        
        RestAssured.given()
                .header("Authorization", "Bearer " + invalidToken)
                .queryParam("username", "test@gmail.com")
                .when()
                .get("/api/v1/resetpw")
                .then()
                .statusCode(401);
    }

    // Success Test for reset password not possible because of the console input
}
