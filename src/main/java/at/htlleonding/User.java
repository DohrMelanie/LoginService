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
@AllArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$\n", message = "Email must be valid")
    private String username;

    @NotNull
    @Pattern(regexp= "^(\\+|)\\d+$\n", message = "Telephone number must be valid")
    private String telephoneNumber;

    @NotNull
    private String password;
}
