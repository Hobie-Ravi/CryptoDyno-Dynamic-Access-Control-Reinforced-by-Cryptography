package com.CryptoDyno.Payload;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileGDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime creationDate;
}
