package at.htlleonding;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;
import static org.wildfly.common.Assert.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    LoginPanacheRepository loginRepo;

    @InjectMocks
    static LoginService loginService;

    private static User sampleUser;

    @BeforeEach
    public void setUpAll() {
        sampleUser = new User("testUser", LoginService.getPassword("secret"), "12345");
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
            verify(loginRepo).persist(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when password is empty")
        void testAddUser_EmptyPassword() {
            String password = sampleUser.getPassword();
            sampleUser.setPassword("");
            assertThrows(IllegalArgumentException.class, () -> loginService.addUser(sampleUser));
            verify(loginRepo, never()).persist(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when username is empty")
        void testAddUser_EmptyUsername() {
            String username = sampleUser.getUsername();
            sampleUser.setUsername("");
            assertThrows(IllegalArgumentException.class, () -> loginService.addUser(sampleUser));
            verify(loginRepo, never()).persist(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when telephone number is empty")
        void testAddUser_EmptyTelephoneNumber() {
            String telephoneNumber = sampleUser.getTelephoneNumber();
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
    }
}
