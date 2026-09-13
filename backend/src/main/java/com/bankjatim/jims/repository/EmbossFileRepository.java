package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.EmbossFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmbossFileRepository extends JpaRepository<EmbossFile, Long> {
    List<EmbossFile> findAllByOrderByCreatedAtDesc();
}
