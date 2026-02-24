package com.example.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.repository.ExpensesRepository;
import com.example.repository.RevenuesRepository;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {
    @Autowired
    private RevenuesRepository revenueRepository;

    @Autowired
    private ExpensesRepository expensesRepository;

    @GetMapping("/monthly-summary")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlySummary() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        BigDecimal totalRevenue = revenueRepository.sumExpectedByMonth(year, month);
        BigDecimal totalExpenses = expensesRepository.sumByMonthAndType(year, month);
        BigDecimal balance = totalRevenue.subtract(totalExpenses);
            
        return ResponseEntity.ok(Map.of(
                "totalRevenue", totalRevenue,
                "totalExpenses", totalExpenses,
                "balance", balance));
    }

}
