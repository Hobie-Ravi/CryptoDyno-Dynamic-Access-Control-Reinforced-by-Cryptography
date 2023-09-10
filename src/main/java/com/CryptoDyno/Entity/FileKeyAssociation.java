package com.CryptoDyno.Entity;

import javax.persistence.*;

@Entity
@Table(name = "File_Key_Association")
public class FileKeyAssociation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "file_id")
    private FileEntity file;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "key_id")
    private KeyRotateEntity key;

    @Column(name = "reencryption_count")
    private int reencryptionCount;


    public FileKeyAssociation() {
    }

    public FileKeyAssociation(Long id, FileEntity file, KeyRotateEntity key) {
        this.id = id;
        this.file = file;
        this.key = key;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FileEntity getFile() {
        return file;
    }

    public void setFile(FileEntity file) {
        this.file = file;
    }

    public KeyRotateEntity getKey() {
        return key;
    }

    public void setKey(KeyRotateEntity key) {
        this.key = key;
    }

    public int getReencryptionCount() {
        return reencryptionCount;
    }

    public void setReencryptionCount(int reencryptionCount) {
        this.reencryptionCount = reencryptionCount;
    }
    public void incrementReencryptionCount() {
        reencryptionCount++;
    }
}
