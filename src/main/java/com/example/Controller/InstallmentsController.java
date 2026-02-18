package com.example.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.models.Expenses;
import com.example.repository.ExpensesRepository;
import com.example.service.InstallmentSchedulerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/installments")
@CrossOrigin("*")
public class InstallmentsController {

    @Autowired
    private ExpensesRepository expensesRepository;

    @Autowired
    private InstallmentSchedulerService installmentSchedulerService;

    // GET /api/installments - Listar todas as parcelas
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllInstallments() {
        List<Expenses> expenses = expensesRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Expenses expense : expenses) {
            if ("PARCELADO".equals(expense.getType()) || "FIXO".equals(expense.getType())) {
                int total = expense.getTotalInstallments() != null ? expense.getTotalInstallments() : 1;
                for (int i = 1; i <= total; i++) {
                    boolean isPaid = expense.getCurrentInstallment() != null && expense.getCurrentInstallment() >= i;
                    Map<String, Object> installment = new HashMap<>();
                    installment.put("id", expense.getId() * 100 + i);
                    installment.put("expenseId", expense.getId());
                    installment.put("description", expense.getName());
                    installment.put("amount",
                            expense.getInstallmentAmount() != null ? expense.getInstallmentAmount() : BigDecimal.ZERO);
                    installment.put("installmentNumber", i);
                    installment.put("totalInstallments", total);
                    installment.put("paid", isPaid);
                    installment.put("dueDate",
                            expense.getRegisteredAt() != null ? expense.getRegisteredAt().plusMonths(i - 1).toString()
                                    : "");
                    result.add(installment);
                }
            }
        }

        return ResponseEntity.ok(result);
    }

    // GET /api/installments/pending - Listar parcelas pendentes
    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingInstallments() {
        List<Expenses> expenses = expensesRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Expenses expense : expenses) {
            if ("PENDENTE".equals(expense.getStatus()) &&
                    ("PARCELADO".equals(expense.getType()) || "FIXO".equals(expense.getType()))) {
                int current = expense.getCurrentInstallment() != null ? expense.getCurrentInstallment() : 0;
                int total = expense.getTotalInstallments() != null ? expense.getTotalInstallments() : 1;
                for (int i = current + 1; i <= total; i++) {
                    Map<String, Object> installment = new HashMap<>();
                    installment.put("id", expense.getId() * 100 + i);
                    installment.put("expenseId", expense.getId());
                    installment.put("description", expense.getName());
                    installment.put("amount",
                            expense.getInstallmentAmount() != null ? expense.getInstallmentAmount() : BigDecimal.ZERO);
                    installment.put("installmentNumber", i);
                    installment.put("totalInstallments", total);
                    installment.put("paid", false);
                    installment.put("dueDate",
                            expense.getRegisteredAt() != null ? expense.getRegisteredAt().plusMonths(i - 1).toString()
                                    : "");
                    result.add(installment);
                }
            }
        }

        return ResponseEntity.ok(result);
    }

    // GET /api/installments/expense/{expenseId} - Parcelas de uma despesa
    // específica
    @GetMapping("/expense/{expenseId}")
    public ResponseEntity<List<Map<String, Object>>> getInstallmentsByExpense(@PathVariable Long expenseId) {
        return expensesRepository.findById(expenseId)
                .map(expense -> {
                    List<Map<String, Object>> result = new ArrayList<>();
                    int total = expense.getTotalInstallments() != null ? expense.getTotalInstallments() : 1;
                    for (int i = 1; i <= total; i++) {
                        boolean isPaid = expense.getCurrentInstallment() != null
                                && expense.getCurrentInstallment() >= i;
                        Map<String, Object> installment = new HashMap<>();
                        installment.put("id", expense.getId() * 100 + i);
                        installment.put("expenseId", expense.getId());
                        installment.put("description", expense.getName());
                        installment.put("amount",
                                expense.getInstallmentAmount() != null ? expense.getInstallmentAmount()
                                        : BigDecimal.ZERO);
                        installment.put("installmentNumber", i);
                        installment.put("totalInstallments", total);
                        installment.put("paid", isPaid);
                        installment.put("dueDate",
                                expense.getRegisteredAt() != null
                                        ? expense.getRegisteredAt().plusMonths(i - 1).toString()
                                        : "");
                        result.add(installment);
                    }
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/installments/month/{year}/{month} - Parcelas de um mês específico
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<Map<String, Object>>> getInstallmentsByMonth(@PathVariable int year,
            @PathVariable int month) {
        List<Expenses> expenses = expensesRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Expenses expense : expenses) {
            if (("PARCELADO".equals(expense.getType()) || "FIXO".equals(expense.getType()))
                    && expense.getRegisteredAt() != null) {
                int total = expense.getTotalInstallments() != null ? expense.getTotalInstallments() : 1;
                for (int i = 1; i <= total; i++) {
                    var dueDate = expense.getRegisteredAt().plusMonths(i - 1);
                    if (dueDate.getYear() == year && dueDate.getMonthValue() == month) {
                        boolean isPaid = expense.getCurrentInstallment() != null
                                && expense.getCurrentInstallment() >= i;
                        Map<String, Object> installment = new HashMap<>();
                        installment.put("id", expense.getId() * 100 + i);
                        installment.put("expenseId", expense.getId());
                        installment.put("description", expense.getName());
                        installment.put("amount",
                                expense.getInstallmentAmount() != null ? expense.getInstallmentAmount()
                                        : BigDecimal.ZERO);
                        installment.put("installmentNumber", i);
                        installment.put("totalInstallments", total);
                        installment.put("paid", isPaid);
                        installment.put("dueDate", dueDate.toString());
                        result.add(installment);
                    }
                }
            }
        }

        return ResponseEntity.ok(result);
    }

    // POST /api/installments/{id}/pay - Marcar parcela como paga
    @PostMapping("/{id}/pay")
    public ResponseEntity<String> payInstallment(@PathVariable Long id) {
        // O ID da parcela é composto por expenseId * 100 + installmentNumber
        long expenseId = id / 100;
        installmentSchedulerService.processPaymentManual(expenseId);
        return ResponseEntity.ok("PagamentoProcessado");
    }

    // GET /api/installments/summary - Resumo de parcelas
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<Expenses> expenses = expensesRepository.findAll();

        BigDecimal totalPending = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        long pendingCount = 0;

        for (Expenses expense : expenses) {
            if ("PARCELADO".equals(expense.getType()) || "FIXO".equals(expense.getType())) {
                if ("PENDENTE".equals(expense.getStatus()) && expense.getInstallmentAmount() != null) {
                    totalPending = totalPending.add(expense.getInstallmentAmount());
                    pendingCount++;
                }
                if ("PAGO".equals(expense.getStatus()) && expense.getInstallmentAmount() != null) {
                    totalPaid = totalPaid.add(expense.getInstallmentAmount());
                }
            }
        }

        return ResponseEntity.ok(Map.of(
                "totalPending", totalPending,
                "totalPaid", totalPaid,
                "pendingCount", pendingCount));
    }
}
