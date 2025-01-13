package at.htlleonding.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResetPasswordDto {
    private String username;
    private String resetCode;
    private String newPassword;
}
