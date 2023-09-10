package com.CryptoDyno.Entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "File_Details")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "File_Name", unique = true)
    private String fileName;

    @Column(name = "File_Type")
    private String fileType;

    @Column(name = "File_Size")
    private long fileSize;

    @Column(name = "Creation_Date")
    private LocalDateTime creationDate;

    @Column(name = "User_Mail_ID")
    private String userName;


    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileKeyAssociation> fileAssociations = new ArrayList<>();

    public FileEntity() {
    }

    public FileEntity(String fileName, String fileType, long fileSize, LocalDateTime creationDate, String userName) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.creationDate = creationDate;
        this.userName = userName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<FileKeyAssociation> getFileAssociations() {
        return fileAssociations;
    }

    public void setFileAssociations(List<FileKeyAssociation> fileAssociations) {
        this.fileAssociations = fileAssociations;
    }

}