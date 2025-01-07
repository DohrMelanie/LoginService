package at.htlleonding;

import io.jsonwebtoken.Header;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.StringReader;
import java.util.Base64;

public class JWTService {
    private static final String SECRET_KEY = System.getenv("SECRET_KEY");

    public static String generateToken(String username, int timeLimit_Min) {
        String header = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());

        long expirationTime = (System.currentTimeMillis() / 1000) + (timeLimit_Min * 60);

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

    public static boolean verifyToken(String token) {
        String[] parts = token.split("\\."); //why it gotta use regexxxxxxxxxxx

        if (parts.length != 3) {
            return false;
        }

        String header = parts[0];
        String payload = parts[1];
        String signature = parts[2];

        try {
            //check signature
            String expectedSignature = encryptHmac256(header + "." + payload);
            
            if(!signature.equals(expectedSignature){
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

    private static String encryptHmac256(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes()));
    }
}
