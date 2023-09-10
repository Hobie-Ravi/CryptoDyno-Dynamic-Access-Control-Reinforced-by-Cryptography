package com.CryptoDyno.Payload;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class LoginDTO {
    @NotEmpty(message = "username or email cannot be empty")
    @Size(min = 3, max = 50, message = "username or email should be between 3 and 50 characters")
    private String usernameOrEmail;

    @NotEmpty(message = "password cannot be empty")
    @Size(min = 8, max = 30, message = "password should be between 8 and 20 characters")
    private String password;
}