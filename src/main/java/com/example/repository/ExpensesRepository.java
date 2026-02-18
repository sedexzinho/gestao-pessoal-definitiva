package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.models.Expenses;

@Repository
public interface ExpensesRepository extends JpaRepository<Expenses, Long> {
   List<Expenses> findByDueDayAndActiveTrue(Integer dueDay);

   List<Expenses> findByActiveTrueAndStatusAndTypeIn(String status, List<String> types);

   List<Expenses> findByActiveTrueAndStatusAndTypeInAndDueDay(String status, List<String> types, Integer dueDay);
}
