package com.example.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.Dto.ExpensesDTO;
import com.example.models.Expenses;
import com.example.repository.ExpensesRepository;
import com.example.service.ExpensesService;
import com.example.service.InstallmentSchedulerService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin("*")
public class ExpensesController {
    @Autowired
    private ExpensesService expensesService;

    @Autowired
    private InstallmentSchedulerService installmentSchedulerService;

    @Autowired
    private ExpensesRepository expensesRepository;

    // GET /api/expenses - Listar todas as despesas
    @GetMapping
    public ResponseEntity<List<Expenses>> listarTodasDespesas() {
        return ResponseEntity.ok(expensesRepository.findAll());
    }

    // GET /api/expenses/{id} - Buscar despesa por ID
    @GetMapping("/{id}")
    public ResponseEntity<Expenses> buscarDespesaPorId(@PathVariable Long id) {
        return expensesRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/expenses - Criar nova despesa
    @PostMapping
    public ResponseEntity<String> registrarDespesa(@RequestBody @Valid ExpensesDTO expensesDTO) {
        expensesService.registrarGasto(expensesDTO);
        return ResponseEntity.ok("DespesaRegistrada");
    }

    // PUT /api/expenses/{id} - Atualizar despesa
    @PutMapping("/{id}")
    public ResponseEntity<Expenses> atualizarDespesa(@PathVariable Long id, @RequestBody ExpensesDTO expensesDTO) {
        return expensesRepository.findById(id)
                .map(expense -> {
                    expense.setName(expensesDTO.nome());
                    expense.setAmount(expensesDTO.valorPago());
                    expense.setType(expensesDTO.tipo());
                    if (expensesDTO.diaVencimento() != null) {
                        expense.setDueDay(expensesDTO.diaVencimento());
                    }
                    if (expensesDTO.totalParcelas() != null) {
                        expense.setTotalInstallments(expensesDTO.totalParcelas());
                    }
                    if (expensesDTO.parcelaAtual() != null) {
                        expense.setCurrentInstallment(expensesDTO.parcelaAtual());
                    }
                    // Buscar categoria pelo nome
                    if (expensesDTO.nomeCategoria() != null) {
                        // A categoria será mantida ou atualizada se necessário
                    }
                    Expenses updated = expensesRepository.save(expense);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/expenses/{id} - Excluir despesa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirDespesa(@PathVariable Long id) {
        if (expensesRepository.existsById(id)) {
            expensesRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // GET /api/expenses/summary - Resumo de despesas
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<Expenses> all = expensesRepository.findAll();

        BigDecimal totalExpenses = all.stream()
                .map(Expenses::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInstallments = all.stream()
                .filter(e -> "PARCELADO".equals(e.getType()) || "FIXO".equals(e.getType()))
                .map(Expenses::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingInstallments = all.stream()
                .filter(e -> "PENDENTE".equals(e.getStatus()))
                .map(Expenses::getInstallmentAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(Map.of(
                "totalExpenses", totalExpenses,
                "totalInstallments", totalInstallments,
                "pendingInstallments", pendingInstallments));
    }

    // GET /api/expenses/month/{year}/{month} - Despesas por mês
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<Expenses>> getExpensesByMonth(@PathVariable int year, @PathVariable int month) {
        List<Expenses> all = expensesRepository.findAll();
        List<Expenses> filtered = all.stream()
                .filter(e -> e.getRegisteredAt() != null)
                .filter(e -> e.getRegisteredAt().getYear() == year)
                .filter(e -> e.getRegisteredAt().getMonthValue() == month)
                .toList();
        return ResponseEntity.ok(filtered);
    }

    // POST /api/expenses/pay/{id} - Pagar parcela (já existe)
    @PostMapping("/pay/{id}")
    public ResponseEntity<String> pagarParcela(@PathVariable Long id) {
        installmentSchedulerService.processPaymentManual(id);
        return ResponseEntity.ok("PagamentoProcessado");
    }
}
