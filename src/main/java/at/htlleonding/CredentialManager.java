package at.htlleonding;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Getter
public class CredentialManager {

    @ConfigProperty(name = "pepper")
    String pepper;

    @ConfigProperty(name = "secret")
    String secret;
}
