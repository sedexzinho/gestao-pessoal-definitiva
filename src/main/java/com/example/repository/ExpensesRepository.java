package com.example.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.models.Expenses;

@Repository
public interface ExpensesRepository extends JpaRepository<Expenses, Long> {
   List<Expenses> findByDueDayAndActiveTrue(Integer dueDay);

   List<Expenses> findByActiveTrueAndStatusAndTypeIn(String status, List<String> types);

   List<Expenses> findByActiveTrueAndStatusAndTypeInAndDueDay(String status, List<String> types, Integer dueDay);

   
   @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expenses e WHERE YEAR(e.registeredAt) = :year AND MONTH(e.registeredAt) = :month")
   BigDecimal sumByMonth(int year, int month);

   // Soma despesas por tipo do mês atual
   @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expenses e WHERE YEAR(e.registeredAt) = :year AND MONTH(e.registeredAt) = :month AND e.type = :type")
   BigDecimal sumByMonthAndType(int year, int month);
}
