package at.htlleonding;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@QuarkusTest
public class LoginResourceTest {

    @Test
    public void testRegisterSuccess() {
        User user = new User("test@gmail.com", "password123", "+123456789");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/api/v1/register")
                .then()
                .statusCode(201);
    }

    @Test
    public void testLoginSuccess() {
        String username = "test@gmail.com";
        String password = "password123";

        RestAssured.given()
                .queryParam("username", username)
                .queryParam("password", password)
                .when()
                .get("/api/v1/login")
                .then()
                .statusCode(200)
                .header("Authorization", startsWith("Bearer"));
    }

    @Test
    public void testLoginFailureInvalidPassword() {
        String username = "test@gmail.com";
        String password = "wrongpassword";

        RestAssured.given()
                .queryParam("username", username)
                .queryParam("password", password)
                .when()
                .get("/api/v1/login")
                .then()
                .statusCode(401);
    }

    @Test
    public void testResetPasswordUnauthorized() {
        RestAssured.given()
                .queryParam("username", "test@gmail.com")
                .when()
                .get("/api/v1/resetpw")
                .then()
                .statusCode(401);
    }

    @Test
    public void testResetPasswordWithInvalidToken() {
        String invalidToken = "invalid.jwt.token";

        RestAssured.given()
                .header("Authorization", "Bearer " + invalidToken)
                .queryParam("username", "test@gmail.com")
                .when()
                .get("/api/v1/resetpw")
                .then()
                .statusCode(401);
    }


    private class User {
        private String username;
        private String password;
        private String telephoneNumber;

        public User() {
        }

        public User(String username, String password, String telephoneNumber) {
            this.username = username;
            this.password = password;
            this.telephoneNumber = telephoneNumber;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getTelephoneNumber() {
            return telephoneNumber;
        }

        public void setTelephoneNumber(String telephoneNumber) {
            this.telephoneNumber = telephoneNumber;
        }
    }
}
