package com.CryptoDyno.Controller;

import com.CryptoDyno.Payload.FileRequestDTO;
import com.CryptoDyno.Service.FileRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/file-requests")
public class FileRequestController {
    private final FileRequestService fileRequestService;

    @Autowired
    public FileRequestController(FileRequestService fileRequestService) {
        this.fileRequestService = fileRequestService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/request/{fileId}")
    public ResponseEntity<String> requestFileAccess(@PathVariable Long fileId,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            fileRequestService.requestFile(fileId, userDetails.getUsername());
            return ResponseEntity.ok("File access request sent!");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{requestId}")
    public ResponseEntity<String> approveFileRequest(@PathVariable Long requestId) {
        fileRequestService.approveFileRequest(requestId);
        return ResponseEntity.ok("File access request approved.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<FileRequestDTO>> getAllFileRequests() {
        List<FileRequestDTO> fileRequestDTOs = fileRequestService.getAllFileRequests();
        return ResponseEntity.ok(fileRequestDTOs);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<String> rejectFileRequest(@PathVariable Long requestId) {
        try {
            fileRequestService.rejectFileRequest(requestId);
            return ResponseEntity.ok("File request rejected successfully.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
