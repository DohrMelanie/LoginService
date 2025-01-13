package at.htlleonding;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@Slf4j
@Transactional
public class LoginService {
    public static class Argon2Singleton {
        private static class Holder {
            private static final Argon2 INSTANCE = Argon2Factory.create();
        }
        private Argon2Singleton() {}
        public static Argon2 getInstance() {
            return Holder.INSTANCE;
        }
    }
    @Inject
    LoginPanacheRepository loginRepo;

    @Inject
     CredentialManager credentialManager;

    public User getUserById(UUID id) {
        log.info("Getting user by id: {}", id);
        return loginRepo.findById(id);
    }

    public void addUser(User user) {
        log.info("Adding user: {}", user.getUsername());
        checkArguments(user);

        if (loginRepo.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists!");
        }
        user.setPassword(encryptPassword(user.getPassword()));
        loginRepo.persist(user);
    }
    
    String encryptPassword(String password) {
        password += password + credentialManager.getPepper();
        Argon2 argon2 = Argon2Factory.create();
        return argon2.hash(2, 65536, 1, password.toCharArray()); // The generated hash includes the salt automatically
    }

    public boolean checkPassword(String username, String password) {
        log.info("Checking password for user: {}", username);
        User user = loginRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException();
        }

        Argon2 argon2 = Argon2Factory.create();
        password += credentialManager.getPepper();
        return argon2.verify(user.getPassword(), password.toCharArray());
    }

    public String resetPassword(String username) {
        log.info("Resetting password for user: {}", username);
        User user = loginRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found!");
        }
        log.info("EMAIL SENDING TO: {}", user.getUsername());
        user.setResetCode(UUID.randomUUID().toString());
        log.info("Email: reset code: {}", user.getResetCode());
        return user.getResetCode();
    }

    public boolean resetPasswordWithCode(String username, String code, String password) {
        log.info("Resetting password for user: {}", username);
        User user = loginRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found!");
        }
        if (user.getResetCode() == null || user.getResetCode().isEmpty()) {
            throw new IllegalArgumentException("No reset code found!");
        }
        if (!user.getResetCode().equals(code)) {
            return false;
        }
        user.setPassword(encryptPassword(password));
        return true;
    }

    public void updateUser(User user) {
        log.info("Updating user: {}", user.getId());
        checkArguments(user);
        loginRepo.updateUser(user);
    }

    private void checkArguments(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username must not be empty");
        }

        if (user.getTelephoneNumber() == null || user.getTelephoneNumber().isEmpty()) {
            throw new IllegalArgumentException("Telephone Number must not be empty");
        }
    }

    public void deleteUser(UUID id) {
        log.info("Deleting user: {}", id);
        User user = loginRepo.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found!");
        }
        loginRepo.deleteUser(user);
    }

    public void deleteUserByName(String username) {
        log.info("Deleting user: {}", username);
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        loginRepo.deleteUserByName(username);
    }
}
