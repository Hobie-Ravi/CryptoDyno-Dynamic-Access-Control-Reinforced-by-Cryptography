package com.CryptoDyno.Payload;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class SignUpDTO {
    @NotEmpty
    @Size(min = 3, max = 50, message = "name should be between 3 and 50 characters")
    private String name;

    @NotEmpty
    @Size(min = 3, max = 20, message = "username should be between 3 and 20 characters")
    private String username;

    @NotEmpty
    @Email(message = "invalid email format")
    private String email;

    @NotEmpty
    @Size(min = 8, max = 30, message = "password should be between 8 and 20 characters")
    private String password;
}
