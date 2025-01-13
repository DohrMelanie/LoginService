package at.htlleonding;


import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CredentialManager {

    @ConfigProperty(name = "pepper")
    private String pepper;

    @ConfigProperty(name = "secret")
    private String secret;

    public String getPepper() {
        return pepper;
    }

    public String getSecret() {
        return secret;
    }
}
