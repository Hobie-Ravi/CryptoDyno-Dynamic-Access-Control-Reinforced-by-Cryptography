package com.CryptoDyno.Service;

import com.CryptoDyno.Email.EmailService;
import com.CryptoDyno.Entity.FileEntity;
import com.CryptoDyno.Entity.FileRequest;
import com.CryptoDyno.Entity.User;
import com.CryptoDyno.Enum.RequestStatus;
import com.CryptoDyno.Payload.FileRequestDTO;
import com.CryptoDyno.Repository.FileRepository;
import com.CryptoDyno.Repository.FileRequestRepository;
import com.CryptoDyno.Repository.UserRepository;
import com.CryptoDyno.Utils.EncryptionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileRequestService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FileService fileService;
    private final FileRequestRepository fileRequestRepository;
    private final EmailService emailService;
    private final EncryptionUtils encryptionUtils;

    @Autowired
    public FileRequestService(ModelMapper modelMapper, UserRepository userRepository, FileRepository fileRepository,
                              FileService fileService, FileRequestRepository fileRequestRepository,
                              EmailService emailService, EncryptionUtils encryptionUtils) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.fileService = fileService;
        this.fileRequestRepository = fileRequestRepository;
        this.emailService = emailService;
        this.encryptionUtils = encryptionUtils;
    }

    public void requestFile(Long fileId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        FileEntity file = fileRepository.findById(fileId).orElse(null);

        if (user != null && file != null) {
            FileRequest request = new FileRequest();
            request.setUser(user);
            request.setFile(file);
            request.setStatus(RequestStatus.PENDING);
            fileRequestRepository.save(request);

            String adminEmail = "ravies728@gmail.com";
            String subject = "File Access Request";
            String body = "Dear Admin,\n\n"
                    + "User " + user.getName() + " has requested access to file: " + file.getFileName() + ".\n"
                    + "Please review and approve the request.\n\n"
                    + "Best regards,\n"
                    + "The Support Team";
            emailService.sendSimpleEmail(adminEmail, subject, body);
        }
    }

    public void approveFileRequest(Long requestId) {
        FileRequest request = fileRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("File request not found with ID: " + requestId));

        if (request.getStatus() == RequestStatus.PENDING) {
            request.setStatus(RequestStatus.APPROVED);
            fileRequestRepository.save(request);

            User user = request.getUser();
            FileEntity file = request.getFile();

            try {
                byte[] decryptedFileContent = fileService.decryptFileContent(file);

                String decryptedFileName = "File-" + file.getFileName();
                String subject = "File Access Approved";
                String body = "Dear " + user.getName() + ",\n\n"
                        + "Your request for file access has been approved.\n\n"
                        + "Please find the decrypted file attached.\n\n"
                        + "Best regards,\n"
                        + "The Support Team";

                emailService.sendEmailWithAttachment(user.getEmail(), subject, body,
                        decryptedFileName, decryptedFileContent);

            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException |
                     InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
        }
    }

    public void rejectFileRequest(Long requestId) {
        Optional<FileRequest> optionalFileRequest = fileRequestRepository.findById(requestId);

        if (optionalFileRequest.isPresent()) {
            FileRequest fileRequest = optionalFileRequest.get();
            fileRequest.setStatus(RequestStatus.REJECTED);

            fileRequestRepository.save(fileRequest);
            sendRejectionEmail(fileRequest.getUser().getEmail());

        } else {
            throw new NoSuchElementException("File request not found with ID: " + requestId);
        }
    }
    public List<FileRequestDTO> getAllFileRequests() {
        List<FileRequest> fileRequests = fileRequestRepository.findAll();
        return fileRequests.stream()
                .map(this::convertToFileRequestDTO)
                .collect(Collectors.toList());
    }

    private FileRequestDTO convertToFileRequestDTO(FileRequest fileRequest) {
        return modelMapper.map(fileRequest, FileRequestDTO.class);
    }

    private void sendRejectionEmail(String userEmail) {
        String subject = "File Request Rejected";
        String body = "Your file request has been rejected by the administrator.";

        emailService.sendSimpleEmail(userEmail, subject, body);
    }
}