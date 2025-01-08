package at.htlleonding;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class LoginPanacheRepository implements PanacheRepositoryBase<User, UUID> {
    public void updateUser(User user) {
        getEntityManager().merge(user);
    }
    public void deleteUser(User user) {
        getEntityManager().remove(user);
    }
    public void deleteUserByName(String username) {
        getEntityManager().remove(findByUsername(username));
    }
    public User findByUsername(String username) {
        return find("username", username).firstResult();
    }
}
