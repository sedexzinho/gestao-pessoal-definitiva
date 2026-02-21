package com.example.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.models.Revenue;

@Repository
public interface RevenuesRepository extends JpaRepository<Revenue, Long> {
   List<Revenue> findByActiveTrueAndStatusAndTypeIn(String status, List<String> type);

   @Query("SELECT r FROM Revenue r WHERE YEAR(r.registeredAt) = :year AND MONTH(r.registeredAt) = :month")
   List<Revenue> findByMonth(int year, int month);

   @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r")
   BigDecimal sumAllAmounts();

   @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE r.type = 'FIXO'")
   BigDecimal sumFixedAmount();

   @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE r.status = 'PENDENTE'")
   BigDecimal sumPendingAmount();

   @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE r.type = 'AVULSO'")
   BigDecimal sumMiscellaneousAmount();

}
