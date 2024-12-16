package at.htlleonding;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

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
        loginRepo.persist(user);
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
        loginRepo.deleteById(id);
    }
}
