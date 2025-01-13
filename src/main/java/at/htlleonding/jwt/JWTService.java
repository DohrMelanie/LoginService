package at.htlleonding.jwt;

import at.htlleonding.CredentialManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.StringReader;
import java.util.Base64;

@ApplicationScoped
public class JWTService {

    @Inject
    private CredentialManager credentialManager;
    
    public String generateToken(String username, int minTimeLimit) {
        String header = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());

        long expirationTime = (System.currentTimeMillis() / 1000) + (minTimeLimit * 60L);

        String payload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        ("{\"username\":\"}" + username + "\",\"exp\":" + expirationTime + "}").getBytes()
                );

        try {
            String signature = encryptHmac256(header + "." + payload);

            return header + "." + payload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Error while creating token", e);
        }
    }

    public boolean verifyToken(String token) {
        String[] parts = token.split("\\.");

        if (parts.length != 3) {
            return false;
        }

        String header = parts[0];
        String payload = parts[1];
        String signature = parts[2];

        try {
            //check signature
            String expectedSignature = encryptHmac256(header + "." + payload);

            if (!signature.equals(expectedSignature)) {
                return false;
            }

            //check expiration date
            String decodedPayload = new String(Base64.getUrlDecoder().decode(payload));
            JsonReader jsonReader = Json.createReader(new StringReader(decodedPayload));
            JsonObject payloadJson = jsonReader.readObject();
            jsonReader.close();

            long exp = payloadJson.getJsonNumber("exp").longValue();
            return System.currentTimeMillis() / 1000 < exp;

        } catch (Exception e) {
            throw new RuntimeException("Error verifying the token", e);
        }
    }

    private String encryptHmac256(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(credentialManager.getPepper().getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes()));
    }
}
