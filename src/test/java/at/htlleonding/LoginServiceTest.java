package at.htlleonding;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.wildfly.common.Assert.assertNotNull;

@ExtendWith(MockitoExtension.class)
@QuarkusTest
public class LoginServiceTest {
    @InjectMock
    LoginPanacheRepository loginRepo;

    @Inject
    LoginService loginService;

    private static User sampleUser;

    private static User sampleUserWithCode;

    @BeforeEach
    public void setUpAll() {
        sampleUser = new User("testUser", loginService.encryptPassword("secret"), "12345");
        sampleUserWithCode = new User("testUser2", loginService.encryptPassword("secret"), "12345");
        sampleUserWithCode.setResetCode("resetCode");
    }
    @Nested
    @DisplayName("getUserById tests")
    class GetUserByIdTests {
        @Test
        @DisplayName("Should return user when found by ID")
        void testGetUserById_Found() {
            when(loginRepo.findById(sampleUser.getId())).thenReturn(sampleUser);

            User foundUser = loginService.getUserById(sampleUser.getId());
            assertNotNull(foundUser);
            assertEquals(sampleUser.getId(), foundUser.getId());
            verify(loginRepo).findById(sampleUser.getId());
        }

        @Test
        @DisplayName("Should return null when user not found")
        void testGetUserById_NotFound() {
            when(loginRepo.findById(sampleUser.getId())).thenReturn(null);

            User foundUser = loginService.getUserById(sampleUser.getId());
            assertNull(foundUser);
            verify(loginRepo).findById(sampleUser.getId());
        }
    }

    @Nested
    @DisplayName("addUser tests")
    class AddUserTests {
        @Test
        @DisplayName("Should add user successfully when valid")
        void testAddUser_Success() {
            loginService.addUser(sampleUser);
            verify(loginRepo).addUser(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when password is empty")
        void testAddUser_EmptyPassword() {
            sampleUser.setPassword("");
            assertThrows(IllegalArgumentException.class, () -> loginService.addUser(sampleUser));
            verify(loginRepo, never()).persist(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when username is empty")
        void testAddUser_EmptyUsername() {
            sampleUser.setUsername("");
            assertThrows(IllegalArgumentException.class, () -> loginService.addUser(sampleUser));
            verify(loginRepo, never()).persist(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when telephone number is empty")
        void testAddUser_EmptyTelephoneNumber() {
            sampleUser.setTelephoneNumber("");
            assertThrows(IllegalArgumentException.class, () -> loginService.addUser(sampleUser));
            verify(loginRepo, never()).persist(any(User.class));
        }
    }

    @Nested
    @DisplayName("checkPassword tests")
    class CheckPasswordTests {
        @Test
        @DisplayName("Should throw exception when user not found")
        void testCheckPassword_UserNotFound() {
            when(loginRepo.findByUsername("notExistingUser")).thenReturn(null);
            assertThrows(IllegalArgumentException.class,
                    () -> loginService.checkPassword("notExistingUser", "somePassword"));
        }

        @Test
        @DisplayName("Should return true when password matches")
        void testCheckPassword_Success() {
            sampleUser.setPassword(loginService.encryptPassword("secret"));

            when(loginRepo.findByUsername("testUser")).thenReturn(sampleUser);
            
            boolean result = loginService.checkPassword("testUser", "secret");
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when password does not match")
        void testCheckPassword_Failure() {
            sampleUser.setPassword(loginService.encryptPassword("secret"));
            when(loginRepo.findByUsername("testUser")).thenReturn(sampleUser);

            boolean result = loginService.checkPassword("testUser", "wrongPassword");
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("resetPassword tests")
    class ResetPasswordTests {
        @Test
        @DisplayName("Should throw exception when user not found")
        void testResetPassword_UserNotFound() {
            when(loginRepo.findByUsername("notExistingUser")).thenReturn(null);
            assertThrows(IllegalArgumentException.class,
                    () -> loginService.resetPassword("notExistingUser"));
        }

        @Test
        @DisplayName("Should return reset code when user found")
        void testResetPassword_Success() {
            when(loginRepo.findByUsername("testUser")).thenReturn(sampleUser);

            String resetCode = loginService.resetPassword("testUser");
            assertNotNull(resetCode);
            assertEquals(sampleUser.getResetCode(), resetCode);
        }

        @Test
        @DisplayName("Should throw exception when user has no reset code")
        void testResetPassword_NoResetCode() {
            sampleUser.setResetCode(null);
            when(loginRepo.findByUsername("testUser")).thenReturn(sampleUser);
            assertThrows(IllegalArgumentException.class,
                    () -> loginService.resetPasswordWithCode("testUser", "resetCode", "newPassword"));
        }

        @Test
        @DisplayName("Should throw exception when invalid reset code")
        void testResetPasswordWithCode_InvalidCode() {
            when(loginRepo.findByUsername("testUser")).thenReturn(sampleUserWithCode);
            assertFalse(loginService.resetPasswordWithCode("testUser", "invalidCode", "newPassword"));
        }

        @Test
        @DisplayName("Should reset password successfully with valid reset code")
        void testResetPasswordWithCode_Success() {
            when(loginRepo.findByUsername("testUser2")).thenReturn(sampleUserWithCode);
            assertTrue(loginService.resetPasswordWithCode("testUser2", "resetCode", "newPassword"));
        }
    }

    @Nested
    @DisplayName("updateUser tests")
    class UpdateUserTests {
        @Test
        @DisplayName("Should update user successfully with valid data")
        void testUpdateUser_Success() {
            loginService.updateUser(sampleUser);
            verify(loginRepo).updateUser(sampleUser);
        }

        @Test
        @DisplayName("Should throw exception when required fields are missing")
        void testUpdateUser_MissingFields() {
            sampleUser.setUsername("");
            assertThrows(IllegalArgumentException.class, () -> loginService.updateUser(sampleUser));
            verify(loginRepo, never()).updateUser(any(User.class));
        }
    }

    @Nested
    @DisplayName("deleteUser tests")
    class DeleteUserTests {
        @Test
        @DisplayName("Should delete user if found")
        void testDeleteUser_Found() {
            when(loginRepo.findById(sampleUser.getId())).thenReturn(sampleUser);
            loginService.deleteUser(sampleUser.getId());
            verify(loginRepo).deleteUser(sampleUser);
        }

        @Test
        @DisplayName("Should throw exception if user not found")
        void testDeleteUser_NotFound() {
            when(loginRepo.findById(sampleUser.getId())).thenReturn(null);
            assertThrows(IllegalArgumentException.class, () -> loginService.deleteUser(sampleUser.getId()));
            verify(loginRepo, never()).deleteUser(any(User.class));
        }

        @Test
        @DisplayName("Should delete user by username")
        void testDeleteUserByName() {
            loginService.deleteUserByName(sampleUser.getUsername());
            verify(loginRepo).deleteUserByName(sampleUser.getUsername());
        }

        @Test
        @DisplayName("Should throw exception if username is empty")
        void testDeleteUserByName_EmptyUsername() {
            sampleUser.setUsername("");
            assertThrows(IllegalArgumentException.class, () -> loginService.deleteUserByName(sampleUser.getUsername()));
            verify(loginRepo, never()).deleteUserByName(any(String.class));
        }
    }
}
