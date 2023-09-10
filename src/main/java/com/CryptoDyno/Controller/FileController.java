package com.CryptoDyno.Controller;

import com.CryptoDyno.Entity.FileEntity;
import com.CryptoDyno.Payload.FileDTO;
import com.CryptoDyno.Payload.FileGDTO;
import com.CryptoDyno.Repository.FileAssociationRepository;
import com.CryptoDyno.Service.FileService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;
    private final ModelMapper modelMapper;
    private final FileAssociationRepository fileAssociationRepository;

    @Autowired
    public FileController(FileService fileService, ModelMapper modelMapper, FileAssociationRepository fileAssociationRepository) {
        this.fileService = fileService;
        this.modelMapper = modelMapper;
        this.fileAssociationRepository = fileAssociationRepository;
    }

    //http://localhost:8080/files/upload
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            FileDTO fileDTO = fileService.saveFile(file);
            return ResponseEntity.ok("File uploaded successfully! File ID: " + fileDTO.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload the file.");
        }
    }

    //http://localhost:8080/files/delete/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> deleteFileById(@PathVariable Long id) {
        if (fileService.deleteFileById(id)) {
            return ResponseEntity.ok("File deleted successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the file.");
        }
    }

    //http://localhost:8080/files/re-encrypt/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/re-encrypt/{id}")
    public ResponseEntity<FileDTO> updateFileContent(@PathVariable Long id, @RequestParam("file") MultipartFile newFile) {
        try {
            FileDTO updatedFile = fileService.updateFileContent(id, newFile);
            return ResponseEntity.ok(updatedFile);
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //http://localhost:8080/files/list
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<List<FileDTO>> listAllFiles() {
        List<FileDTO> fileDTOs = fileService.getAllFiles();
        return ResponseEntity.ok(fileDTOs);
    }

    //http://localhost:8080/files/all-list?pageNo=0&pageSize=10&sortBy=fileName&sortDir=desc
    //http://localhost:8080/files/all-list?&pageSize=5
    //http://localhost:8080/files/all-list?pageNo=0
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/all-list")
    public ResponseEntity<Page<FileGDTO>> listFilesWithPaginationAndSorting(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        Page<FileGDTO> filesPage = fileService.getAllFiles(pageNo, pageSize, sortBy, sortDir);
        return ResponseEntity.ok(filesPage);
    }

    //http://localhost:8080/files/decrypt/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/decrypt/{id}")
    public ResponseEntity<String> decryptAndDownloadFile(@PathVariable Long id) {
        try {
            FileEntity fileEntity = fileService.getFileEntityById(id);

            if (fileEntity == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] decryptedFileContent = fileService.decryptFileContent(fileEntity);

            String uniqueIdentifier = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            String decryptedFileName = "decrypted-" + id + "-" + uniqueIdentifier + "-" + fileEntity.getFileName();


            String downloadDirectory = "D:/storage/CryptoDyno/download/";
            String filePath = downloadDirectory + decryptedFileName;

            File downloadDir = new File(downloadDirectory);
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(decryptedFileContent);
            fileOutputStream.close();

            return ResponseEntity.ok("File decrypted and saved successfully!");
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // http://localhost:8080/files/get/{id}
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/get/{id}")
    public ResponseEntity<FileGDTO> getFileById(@PathVariable Long id) {
        FileGDTO fileGDTO = fileService.getFileById(id);

        if (fileGDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fileGDTO);
    }
}
