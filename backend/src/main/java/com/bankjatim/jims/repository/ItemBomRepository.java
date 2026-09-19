package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.ItemBom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemBomRepository extends JpaRepository<ItemBom, Long> {

    List<ItemBom> findByFinishedItemId(Long finishedItemId);

    List<ItemBom> findByMaterialItemId(Long materialItemId);

    Optional<ItemBom> findByFinishedItemIdAndMaterialItemId(Long finishedItemId, Long materialItemId);

    @Query("SELECT b FROM ItemBom b JOIN FETCH b.finishedItem JOIN FETCH b.materialItem ORDER BY b.finishedItem.name ASC")
    List<ItemBom> findAllWithDetails();
}
