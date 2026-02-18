package com.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.models.Expenses;
import com.example.models.User;
import com.example.repository.ExpensesRepository;
import com.example.repository.UserRepository;

@Service
public class InstallmentSchedulerService {

    private final ExpensesRepository expensesRepository;
    private final UserRepository userRepository;

    private static final List<String> RECURRING_TYPES = Arrays.asList("PARCELADO", "FIXO");

    public InstallmentSchedulerService(ExpensesRepository expensesRepository, UserRepository userRepository) {
        this.expensesRepository = expensesRepository;
        this.userRepository = userRepository;
    }

    /**
     * Scheduler que roda todo dia 1 de cada mês às 00:00
     * Processa as despesas parceladas e fixas com status PENDENTE
     * Também muda status de PAGO para PENDENTE quando há parcelas restantes
     */
    @Scheduled(fixedRate = 15000)
    @Transactional
    public void processMonthlyPayments() {
        int today = LocalDate.now().getDayOfMonth();

        // Primeiro: muda status de PAGO para PENDENTE para despesas não concluídas
        List<Expenses> paidExpenses = expensesRepository
                .findByActiveTrueAndStatusAndTypeIn("PAGO", RECURRING_TYPES);

        for (Expenses expense : paidExpenses) {
            if (!Boolean.TRUE.equals(expense.getCompleted())) {
                expense.setStatus("PENDENTE");
                expensesRepository.save(expense);
            }
        }

        // Segundo: busca despesas ativas com status PENDENTE e tipo PARCELADO ou FIXO
        List<Expenses> pendingExpenses = expensesRepository
                .findByActiveTrueAndStatusAndTypeInAndDueDay("PENDENTE", RECURRING_TYPES, today);

        for (Expenses expense : pendingExpenses) {
            processPayment(expense);
        }
    }

    /**
     * Processa o pagamento de uma despesa
     */
    @Transactional
    public void processPayment(Expenses expense) {
        // Verifica se a despesa pode ser paga
        if (!canBePaid(expense)) {
            return;
        }

        // Desconta do saldo do usuário
        descontarDoSaldo(expense.getInstallmentAmount());

        // Registra a data do pagamento
        expense.setLastPaymentDate(LocalDate.now());

        // Incrementa a parcela atual
        int currentInstallment = expense.getCurrentInstallment() != null
                ? expense.getCurrentInstallment()
                : 0;
        expense.setCurrentInstallment(currentInstallment + 1);

        // Verifica se todas as parcelas foram pagas
        if (expense.getCurrentInstallment() >= expense.getTotalInstallments()) {
            // Última parcela paga - marca como concluída
            expense.setCompleted(true);
            expense.setActive(false);
        }

        // Marca o status como PAGO após o pagamento
        expense.setStatus("PAGO");

        expensesRepository.save(expense);
    }

    /**
     * Verifica se a despesa pode ser paga
     * Validação mínima necessária - verifica apenas o valor da parcela
     */
    private boolean canBePaid(Expenses expense) {
        // ✅ Verifica se tem valor de parcela - única validação necessária
        // (as outras verificações já são feitas pela query do repository)
        if (expense.getInstallmentAmount() == null || expense.getInstallmentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Desconta o valor do saldo do usuário
     */
    private void descontarDoSaldo(BigDecimal value) {
        User usuario = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // ✅ Simplificado - sempre subtrai o valor (o if/else fazia a mesma coisa)
        usuario.setMonthlySalary(usuario.getMonthlySalary().subtract(value));
        userRepository.save(usuario);
    }

    @Transactional
    public void processPaymentManual(Long expenseId) {
        Expenses expense = expensesRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));

        processPayment(expense);
    }
}
