package com.CryptoDyno.Service;

import com.CryptoDyno.Email.EmailService;
import com.CryptoDyno.Entity.User;
import com.CryptoDyno.Repository.FileRepository;
import com.CryptoDyno.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final FileRepository fileRepository;
    private final FileService fileService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, FileRepository fileRepository, FileService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.fileRepository = fileRepository;
        this.fileService = fileService;
    }


    public boolean updateUserPassword(Long id, String newPassword) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            userRepository.save(user);

            sendPasswordUpdateEmail(user.getEmail(), newPassword);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendPasswordUpdateEmail(String email, String newPassword) {
        String subject = "Password Update Notification";
        String body = "Dear User,\n\n"
                + "We want to inform you that your account password has been updated.\n\n"
                + "Your new password is: " + newPassword + "\n\n"
                + "Please keep this password confidential and do not share it with anyone.\n"
                + "If you did not initiate this password change, please contact our support team immediately.\n\n"
                + "Thank you for using our services.\n\n"
                + "Best regards,\n"
                + "The Support Team";
        emailService.sendSimpleEmail(email, subject, body);
    }


    @Transactional
    public boolean deactivateUserById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

            user.setActive(false);
            userRepository.save(user);
            sendUserDeactivationEmail(user);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendUserDeactivationEmail(User user) {
        String subject = "Account Deactivation";
        String body = "Dear " + user.getName() + ",\n\n"
                + "We regret to inform you that your account has been deactivated by an administrator. "
                + "This action may have been taken due to a violation of our terms of service or other "
                + "policy violations.\n\n"
                + "If you believe this deactivation is in error, or if you have any questions about the "
                + "deactivation process, please contact our support team. We'll be happy to assist you.\n\n"
                + "If you wish to appeal the deactivation decision, please provide any relevant information "
                + "and our team will review your case.\n\n"
                + "Thank you for your understanding.\n\n"
                + "Best regards,\n"
                + "The Support Team";
        emailService.sendSimpleEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public boolean reactivateUserById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
            user.setActive(true);
            userRepository.save(user);
            sendUserReactivationEmail(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendUserReactivationEmail(User user) {
        String subject = "Account Reactivation";
        String body = "Dear " + user.getName() + ",\n\n"
                + "We're pleased to inform you that your account has been reactivated by an administrator.\n\n"
                + "You can now access your account and continue using all the features and services "
                + "as usual. \n"
                + "If you have any questions or concerns, please feel free to reach out to our "
                + "support team.\n\n"
                + "Thank you for being a valued member of our community!\n\n"
                + "Best regards,\n"
                + "The Support Team";
        emailService.sendSimpleEmail(user.getEmail(), subject, body);
    }
}