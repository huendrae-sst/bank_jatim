package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findBySku(String sku);

    Optional<Item> findByBarcode(String barcode);

    List<Item> findByIsActiveTrueOrderByNameAsc();

    @Query("SELECT i FROM Item i JOIN FETCH i.category ORDER BY i.sku ASC")
    List<Item> findAllWithCategory();

    @Query("SELECT i FROM Item i WHERE (:query IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.sku) LIKE LOWER(CONCAT('%', :query, '%'))) AND (:categoryId IS NULL OR i.category.id = :categoryId)")
    Page<Item> searchItems(@Param("query") String query, @Param("categoryId") Long categoryId, Pageable pageable);
}
