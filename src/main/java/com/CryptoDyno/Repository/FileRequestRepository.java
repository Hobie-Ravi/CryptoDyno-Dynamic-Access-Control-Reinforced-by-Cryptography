package com.CryptoDyno.Repository;

import com.CryptoDyno.Entity.FileRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRequestRepository extends JpaRepository<FileRequest, Long> {
}
