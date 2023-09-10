package com.CryptoDyno.Controller;

import com.CryptoDyno.Email.EmailService;
import com.CryptoDyno.Entity.Role;
import com.CryptoDyno.Entity.User;
import com.CryptoDyno.Payload.LoginDTO;
import com.CryptoDyno.Payload.SignUpDTO;
import com.CryptoDyno.Repository.RoleRepository;
import com.CryptoDyno.Repository.UserRepository;
import com.CryptoDyno.Service.FileService;
import com.CryptoDyno.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;

@RestController
@RequestMapping("/files/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @PostMapping("/sign-in")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDTO loginDTO, BindingResult result) {
        if(result.hasErrors()){
            return new ResponseEntity<>(result.getFieldError().getDefaultMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsernameOrEmail(), loginDTO.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return ResponseEntity.ok("User signed in successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignUpDTO signUpDTO, BindingResult result) {
        if(result.hasErrors()){
            return new ResponseEntity<>(result.getFieldError().getDefaultMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            // Check if the user is an admin
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                // Admin is allowed to add users
                if (userRepository.existsByUsername(signUpDTO.getUsername())) {
                    return ResponseEntity.badRequest().body("Username is already taken");
                }

                if (userRepository.existsByEmail(signUpDTO.getEmail())) {
                    return ResponseEntity.badRequest().body("Email is already taken");
                }

                User user = new User(
                        signUpDTO.getName(),
                        signUpDTO.getUsername(),
                        signUpDTO.getEmail(),
                        passwordEncoder.encode(signUpDTO.getPassword())
                );

                Role userRole = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("Role not found"));
                user.setRoles(Collections.singleton(userRole));

                userRepository.save(user);

                String emailBody = "Hello " + signUpDTO.getName() + ",\n\n"
                        + "We are delighted to welcome you to our platform! Your account has been successfully registered.\n\n"
                        + "Here are your account details:\n\n"
                        + "Username: " + signUpDTO.getUsername() + "\n"
                        + "Password: " + signUpDTO.getPassword() + "\n\n"
                        + "Please make sure to keep your credentials secure and do not share them with anyone.\n\n"
                        + "With your account, you can start uploading and managing your files securely.\n\n"
                        + "If you have any questions or need assistance, feel free to contact our support team.\n\n"
                        + "Thank you for choosing our services.\n\n"
                        + "Best regards,\n"
                        + "The Support Team";

                emailService.sendSimpleEmail(signUpDTO.getEmail(), "Account Registration", emailBody);

                return ResponseEntity.ok("User registered successfully by admin");
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only admin can add users");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updatePassword(@PathVariable Long id, @RequestParam String newPassword) {
        if (userService.updateUserPassword(id, newPassword)) {
            return ResponseEntity.ok("User password updated successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update the user password.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/deactivate/{userId}")
    public ResponseEntity<String> deactivateUser(@PathVariable Long userId) {
        if (userService.deactivateUserById(userId)) {
            return ResponseEntity.ok("User deactivated successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to deactivate the user.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reactivate/{userId}")
    public ResponseEntity<String> reactivateUser(@PathVariable Long userId) {
        if (userService.reactivateUserById(userId)) {
            return ResponseEntity.ok("User reactivated successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reactivate the user.");
        }
    }
}