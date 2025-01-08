package at.htlleonding;

import at.htlleonding.LoginPanacheRepository;
import at.htlleonding.LoginService;
import at.htlleonding.User;
import de.mkammerer.argon2.Argon2Factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.wildfly.common.Assert.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    LoginPanacheRepository loginRepo;

    @InjectMocks
    LoginService loginService;

    private User sampleUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleUser = new User();
        sampleUser.setId(userId);
        sampleUser.setUsername("testUser");
        sampleUser.setPassword("secret");
        sampleUser.setTelephoneNumber("12345");
    }

    @Nested
    @DisplayName("getUserById tests")
    class GetUserByIdTests {
        @Test
        @DisplayName("Should return user when found by ID")
        void testGetUserById_Found() {
            when(loginRepo.findById(userId)).thenReturn(sampleUser);

            User foundUser = loginService.getUserById(userId);
            assertNotNull(foundUser);
            assertEquals(userId, foundUser.getId());
            verify(loginRepo).findById(userId);
        }

        @Test
        @DisplayName("Should return null when user not found")
        void testGetUserById_NotFound() {
            when(loginRepo.findById(userId)).thenReturn(null);

            User foundUser = loginService.getUserById(userId);
            assertNull(foundUser);
            verify(loginRepo).findById(userId);
        }
    }

    @Nested
    @DisplayName("addUser tests")
    class AddUserTests {
        @Test
        @DisplayName("Should add user successfully when valid")
        void testAddUser_Success() {
            loginService.addUser(sampleUser);
            verify(loginRepo).persist(any(User.class));
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
            sampleUser.setPassword(loginService.getPassword("secret"));

            when(loginRepo.findByUsername("testUser")).thenReturn(sampleUser);
            
            boolean result = loginService.checkPassword("testUser", "secret");
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when password does not match")
        void testCheckPassword_Failure() {
            sampleUser.setPassword(loginService.getPassword("secret"));
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
            when(loginRepo.findById(userId)).thenReturn(sampleUser);

            loginService.deleteUser(userId);
            verify(loginRepo).deleteUser(sampleUser);
        }

        @Test
        @DisplayName("Should throw exception if user not found")
        void testDeleteUser_NotFound() {
            when(loginRepo.findById(userId)).thenReturn(null);

            assertThrows(IllegalArgumentException.class, () -> loginService.deleteUser(userId));
            verify(loginRepo, never()).deleteUser(any(User.class));
        }
    }
}
