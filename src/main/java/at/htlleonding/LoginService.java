package at.htlleonding;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
@Slf4j
@Transactional
public class LoginService {
    @Inject
    LoginPanacheRepository loginRepo;

    public User getUserById(UUID id) {
        log.info("Getting user by id: {}", id);
        return loginRepo.findById(id);
    }

    public void addUser(User user) {
        log.info("Adding user: {}", user.getId());
        if (user.getPassword() == null || user.getPassword().isEmpty()
            || user.getUsername() == null || user.getUsername().isEmpty()
            || user.getTelephoneNumber() == null || user.getTelephoneNumber().isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        user.setPassword(getPassword(user.getPassword()));
        loginRepo.persist(user);
    }
    // Generate a random 16-byte salt and encode it as a Base64 string
    private static String generateRandomSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] saltBytes = new byte[16];
        secureRandom.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
    private String getPassword(String password) {
        Argon2 argon2 = Argon2Factory.create();
        String hash = argon2.hash(2, 65536, 1, password.toCharArray());
        hash += generateRandomSalt() + System.getenv("PEPPER");
        return hash;
    }

    public boolean checkPassword(String username, String password) {
        log.info("Checking password for user: {}", username);
        User user = loginRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found!");
        }
        Argon2 argon2 = Argon2Factory.create();
        return argon2.verify(user.getPassword(), password.toCharArray());
    }
    public void resetPassword(String username) {
        log.info("Resetting password for user: {}", username);
        User user = loginRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found!");
        }
        log.info("Enter new password: ");
        user.setPassword(Arrays.toString(System.console().readPassword()));
    }

    public void updateUser(User user) {
        log.info("Updating user: {}", user.getId());
        if (user.getPassword() == null || user.getPassword().isEmpty()
            || user.getUsername() == null || user.getUsername().isEmpty()
            || user.getTelephoneNumber() == null || user.getTelephoneNumber().isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        loginRepo.updateUser(user);
    }
    public void deleteUser(UUID id) {
        log.info("Deleting user: {}", id);
        User user = loginRepo.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("Car not found!");
        }
        loginRepo.deleteUser(user);
    }
}
