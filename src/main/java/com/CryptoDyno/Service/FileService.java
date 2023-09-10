package com.CryptoDyno.Service;

import com.CryptoDyno.Entity.FileEntity;
import com.CryptoDyno.Entity.FileKeyAssociation;
import com.CryptoDyno.Entity.KeyRotateEntity;
import com.CryptoDyno.Payload.FileDTO;
import com.CryptoDyno.Payload.FileGDTO;
import com.CryptoDyno.Repository.FileAssociationRepository;
import com.CryptoDyno.Repository.FileRepository;
import com.CryptoDyno.Repository.KeyRotateRepository;
import com.CryptoDyno.Utils.EncryptionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final KeyRotateRepository keyRotateRepository;
    private final FileAssociationRepository fileAssociationRepository;
    private final EncryptionUtils encryptionUtils;
    private final ModelMapper modelMapper;

    @Autowired
    public FileService(FileRepository fileRepository, KeyRotateRepository keyRotateRepository,
                       FileAssociationRepository fileAssociationRepository, FileAssociationRepository fileKeyAssociationRepository, EncryptionUtils encryptionUtils,
                       ModelMapper modelMapper) {
        this.fileRepository = fileRepository;
        this.keyRotateRepository = keyRotateRepository;
        this.fileAssociationRepository = fileAssociationRepository;
        this.encryptionUtils = encryptionUtils;
        this.modelMapper = modelMapper;
    }

    @Transactional(rollbackOn = Exception.class)
    public FileDTO saveFile(MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String fileName = file.getOriginalFilename();
        String fileType = file.getContentType();
        long fileSize = file.getSize();
        LocalDateTime creationDate = LocalDateTime.now();

        List<KeyRotateEntity> allKeys = keyRotateRepository.findAll();
        KeyRotateEntity selectedKey = allKeys.get(new Random().nextInt(allKeys.size()));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        FileEntity fileEntity = new FileEntity(fileName, fileType, fileSize, creationDate, username);
        FileKeyAssociation association = new FileKeyAssociation();
        association.setFile(fileEntity);
        association.setKey(selectedKey);
        fileAssociationRepository.save(association);

        byte[] encryptedFileContent = encryptionUtils.encryptWithKey(file.getBytes(), selectedKey.getAesKey());

        File storageDirectory = new File("D:/storage/CryptoDyno/");
        if (!storageDirectory.exists()) {
            storageDirectory.mkdirs();
        }

        String filePath = "D:/storage/CryptoDyno/" + fileName;
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            fileOutputStream.write(encryptedFileContent);
        }

        FileEntity savedFileEntity = fileRepository.save(fileEntity);

        FileDTO fileDTO = mapFileEntityToDTO(savedFileEntity);
        return fileDTO;
    }


    public byte[] decryptFileContent(FileEntity fileEntity) throws NoSuchAlgorithmException, InvalidKeyException, IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String encryptionKey = getEncryptionKeyForFile(fileEntity);

        String filePath = "D:/storage/CryptoDyno/" + fileEntity.getFileName();
        File encryptedFile = new File(filePath);
        byte[] encryptedFileContent = org.apache.commons.io.FileUtils.readFileToByteArray(encryptedFile);

        return encryptionUtils.decryptWithKey(encryptedFileContent, encryptionKey);
    }


    @Transactional(rollbackOn = Exception.class)
    public boolean deleteFileById(Long id) {
        FileEntity fileEntity = fileRepository.findById(id).orElse(null);
        if (fileEntity == null) {
            return false;
        }

        String filePath = "D:/storage/CryptoDyno/" + fileEntity.getFileName();
        File fileToDelete = new File(filePath);

        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                fileRepository.deleteById(id);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Transactional(rollbackOn = Exception.class)
    public boolean deleteFileEntityById(Long id) {
        fileRepository.deleteById(id);
        return true;
    }

    public String getEncryptionKeyForFile(FileEntity fileEntity) {
        FileKeyAssociation association = fileAssociationRepository.findByFile(fileEntity);
        if (association != null) {
            KeyRotateEntity keyRotateEntity = association.getKey();
            return keyRotateEntity.getAesKey();
        } else {
            throw new RuntimeException("No encryption key found for the given file.");
        }
    }

    private FileDTO mapFileEntityToDTO(FileEntity fileEntity) {
        return modelMapper.map(fileEntity, FileDTO.class);
    }

    @Transactional(rollbackOn = Exception.class)
    public FileDTO updateFileContent(Long id, MultipartFile newFile) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        FileEntity fileEntity = getFileEntityById(id);

        if (fileEntity == null) {
            throw new IllegalArgumentException("File not found with ID: " + id);
        }

        List<KeyRotateEntity> allKeys = keyRotateRepository.findAll();
        if (allKeys.isEmpty()) {
            throw new IllegalStateException("No keys available for encryption.");
        }

        KeyRotateEntity selectedKey = allKeys.get(new Random().nextInt(allKeys.size()));

        FileKeyAssociation association = fileAssociationRepository.findByFile(fileEntity);

        // Increment the reencryption count
        association.incrementReencryptionCount();

        association.setKey(selectedKey);
        fileAssociationRepository.save(association);

        byte[] encryptedFileContent = encryptionUtils.encryptWithKey(newFile.getBytes(), selectedKey.getAesKey());

        String filePath = "D:/storage/CryptoDyno/" + fileEntity.getFileName();

        // Save the encrypted content to the file
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            fileOutputStream.write(encryptedFileContent);
        }

        // Update fileEntity properties
        fileEntity.setFileSize(newFile.getSize());
        fileEntity.setCreationDate(LocalDateTime.now());

        FileEntity updatedFileEntity = fileRepository.save(fileEntity);

        return mapFileEntityToDTO(updatedFileEntity);
    }


    public List<FileDTO> getAllFiles() {
        List<FileEntity> fileEntities = fileRepository.findAll();
        return fileEntities.stream()
                .map(this::mapFileEntityToDTO)
                .collect(Collectors.toList());
    }

    private FileGDTO mapFileEntityToGDTO(FileEntity fileEntity) {
        FileGDTO fileGDTO = new FileGDTO();
        fileGDTO.setId(fileEntity.getId());
        fileGDTO.setFileName(fileEntity.getFileName());
        fileGDTO.setFileType(fileEntity.getFileType());
        fileGDTO.setFileSize(fileEntity.getFileSize());
        fileGDTO.setCreationDate(fileEntity.getCreationDate());

        return fileGDTO;
    }

    public Page<FileGDTO> getAllFiles(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<FileEntity> fileEntityPage = fileRepository.findAll(pageable);
        return fileEntityPage.map(this::mapFileEntityToGDTO);
    }

    public FileEntity getFileEntityById(Long id) {
        return fileRepository.findById(id).orElse(null);
    }

    public FileGDTO getFileById(Long id) {
        FileEntity fileEntity = fileRepository.findById(id).orElse(null);

        if (fileEntity == null) {
            return null;
        }

        return mapFileEntityToGDTO(fileEntity);
    }
}
