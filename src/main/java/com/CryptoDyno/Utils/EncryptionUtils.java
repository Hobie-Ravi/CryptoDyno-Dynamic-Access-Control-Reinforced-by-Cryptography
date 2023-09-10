package com.CryptoDyno.Utils;

import com.CryptoDyno.Entity.KeyRotateEntity;
import com.CryptoDyno.Repository.KeyRotateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

@Component
public class EncryptionUtils {

    private static final String AES_ALGORITHM = "AES";

    @Autowired
    private KeyRotateRepository keyRotateRepository;

    public String getEncryptionKeyFromDatabase() {

        Long keyId = 1L;
        KeyRotateEntity keyRotateEntity = keyRotateRepository.findById(keyId).orElse(null);
        if (keyRotateEntity != null) {
            return keyRotateEntity.getAesKey();
        } else {
            throw new RuntimeException("Encryption key not found in the database.");
        }
    }

    public byte[] encryptWithKey(byte[] data, String encryptionKey) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(), AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    public byte[] decryptWithKey(byte[] encryptedData, String encryptionKey) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(), AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }

    public KeyRotateEntity getRandomKey() {
        List<KeyRotateEntity> allKeys = keyRotateRepository.findAll();
        return allKeys.get(new Random().nextInt(allKeys.size()));
    }

    public void saveFileContentToSystemDirectory(String fileName, byte[] content) throws IOException {
        File storageDirectory = new File("D:/storage/CryptoDyno/");
        if (!storageDirectory.exists()) {
            storageDirectory.mkdirs();
        }

        String filePath = "D:/storage/CryptoDyno/" + fileName;
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            fileOutputStream.write(content);
        }
    }

    public byte[] getFileContentFromSystemDirectory(String fileName) throws IOException {
        String filePath = "D:/storage/CryptoDyno/" + fileName;
        File encryptedFile = new File(filePath);
        return org.apache.commons.io.FileUtils.readFileToByteArray(encryptedFile);
    }
}