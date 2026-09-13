package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.GeneralLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneralLedgerEntryRepository extends JpaRepository<GeneralLedgerEntry, Long> {
    List<GeneralLedgerEntry> findByReferenceNumber(String referenceNumber);
    List<GeneralLedgerEntry> findByAccountCode(String accountCode);
}
