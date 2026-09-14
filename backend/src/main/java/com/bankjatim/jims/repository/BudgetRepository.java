package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByOrganizationIdAndFiscalYearAndCostCenterCode(Long organizationId, Integer fiscalYear, String costCenterCode);

    List<Budget> findByOrganizationIdAndFiscalYear(Long organizationId, Integer fiscalYear);

    @Query("SELECT b FROM Budget b JOIN FETCH b.organization WHERE b.fiscalYear = :fiscalYear ORDER BY b.costCenterCode ASC")
    List<Budget> findByFiscalYear(@org.springframework.data.repository.query.Param("fiscalYear") Integer fiscalYear);

    @Query("SELECT b FROM Budget b JOIN FETCH b.organization ORDER BY b.fiscalYear DESC, b.costCenterCode ASC")
    List<Budget> findAllWithOrganization();

    @Query(value = """
            SELECT COALESCE(SUM(allocated_amount - committed_amount - realized_amount), 0)
            FROM budgets
            """, nativeQuery = true)
    BigDecimal calculateTotalBudgetRemaining();
}
