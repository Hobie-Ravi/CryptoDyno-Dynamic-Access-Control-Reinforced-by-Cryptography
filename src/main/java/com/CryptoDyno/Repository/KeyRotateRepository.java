package com.CryptoDyno.Repository;

import com.CryptoDyno.Entity.KeyRotateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyRotateRepository extends JpaRepository<KeyRotateEntity, Long> {
}
