package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.models.FixedExpenses;

@Repository
public interface FixedExpensesRepository extends JpaRepository<FixedExpenses, Long>  {
    
}
