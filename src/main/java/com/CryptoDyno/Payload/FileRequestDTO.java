package com.CryptoDyno.Payload;

import com.CryptoDyno.Enum.RequestStatus;
import lombok.Data;

@Data
public class FileRequestDTO {
    private Long id;
    private Long userId;
    private Long fileId;
    private RequestStatus status;

    public FileRequestDTO(Long id,Long userId, Long fileId, RequestStatus status) {
        this.id = id;
        this.userId = userId;
        this.fileId = fileId;
        this.status = status;
    }

    public FileRequestDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
