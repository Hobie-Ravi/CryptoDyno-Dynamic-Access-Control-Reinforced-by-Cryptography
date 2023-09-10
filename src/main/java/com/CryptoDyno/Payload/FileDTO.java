package com.CryptoDyno.Payload;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private long fileSize;
    private LocalDateTime creationDate;
    private String userName;
}
