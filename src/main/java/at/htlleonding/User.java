package at.htlleonding;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Email must be valid") // intended 
    private String username;

    @NotNull
    @Pattern(regexp= "^(\\+|)\\d+$", message = "Telephone number must be valid")
    private String telephoneNumber;

    @NotNull
    private String password;

    private String resetCode;

    public User(String username, String password, String telephoneNumber) {
        this.username = username;
        this.password = password;
        this.telephoneNumber = telephoneNumber;
    }
}
