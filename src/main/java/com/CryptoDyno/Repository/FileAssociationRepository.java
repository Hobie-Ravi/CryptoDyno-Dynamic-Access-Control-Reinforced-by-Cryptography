package com.CryptoDyno.Repository;

import com.CryptoDyno.Entity.FileEntity;
import com.CryptoDyno.Entity.FileKeyAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileAssociationRepository extends JpaRepository<FileKeyAssociation, Long> {
    FileKeyAssociation findByFile(FileEntity file);
}
