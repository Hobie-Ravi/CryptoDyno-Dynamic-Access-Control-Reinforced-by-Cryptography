package com.CryptoDyno.Entity;

import com.CryptoDyno.Enum.RequestStatus;

import javax.persistence.*;

@Entity
@Table(name = "file_requests")
public class FileRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "file_id")
    private FileEntity file;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;


    public FileRequest(Long id, User user, FileEntity file, RequestStatus status) {
        this.id = id;
        this.user = user;
        this.file = file;
        this.status = status;
    }

    public FileRequest() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public FileEntity getFile() {
        return file;
    }

    public void setFile(FileEntity file) {
        this.file = file;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
