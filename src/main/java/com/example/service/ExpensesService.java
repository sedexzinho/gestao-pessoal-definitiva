package com.example.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.Dto.ExpensesDTO;
import com.example.models.Category;
import com.example.models.Expenses;
import com.example.models.User;
import com.example.repository.CategoryRepository;
import com.example.repository.ExpensesRepository;
import com.example.repository.UserRepository;

@Service
public class ExpensesService {

    private UserRepository userRepository;
    private ExpensesRepository expensesRepository;
    private CategoryRepository categoryRepository;

    public ExpensesService(
            UserRepository userRepository,
            ExpensesRepository expensesRepository,
            CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.expensesRepository = expensesRepository;
        this.categoryRepository = categoryRepository;
    }

    public BigDecimal consultarSaldoSimples() {
        BigDecimal salary = userRepository.findById(1L)
                .map(User::getMonthlySalary)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalSpent = expensesRepository.findAll().stream()
                .map(Expenses::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return salary.subtract(totalSpent);
    }

    @Transactional
    public void registrarGasto(ExpensesDTO dto) {
        Category category = categoryRepository.findByNameIgnoreCase(dto.nomeCategoria()).orElseGet(() -> {
            Category nova = new Category();
            nova.setName(dto.nomeCategoria());
            return categoryRepository.save(nova);
        });

        Expenses expenses = mapToEntity(dto, category);
        expensesRepository.save(expenses);

        if (!"PARCELADO".equals(dto.tipo()) && !"FIXO".equals(dto.tipo())) {
            descontarDoSaldo(expenses.getAmount());
        }
    }

    private void descontarDoSaldo(BigDecimal value) {
        User usuario = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        BigDecimal currentBalance = usuario.getMonthlySalary();
        usuario.setMonthlySalary(currentBalance.subtract(value));
        userRepository.save(usuario);
    }

    private Expenses mapToEntity(ExpensesDTO dto, Category category) {
        BigDecimal installmentValue = dto.valorPago().divide(
                BigDecimal.valueOf(dto.totalParcelas()), 2, RoundingMode.HALF_EVEN);
        Expenses expenses = new Expenses();
        expenses.setName(dto.nome());
        expenses.setAmount(dto.valorPago());
        expenses.setCategory(category);
        expenses.setRegisteredAt(LocalDate.now());

        // Para despesas parceladas e fixas, o status inicial é PENDENTE
        // Para despesas avulsas, o status é PAGO
        if ("PARCELADO".equals(dto.tipo()) || "FIXO".equals(dto.tipo())) {
            expenses.setStatus("PENDENTE");
            expenses.setCurrentInstallment(0);
        } else {
            expenses.setStatus("PAGO");
            expenses.setCurrentInstallment(1);
        }

        expenses.setType(dto.tipo());
        // Para despesas parceladas e fixas, ativa por padrão se não especificado
        expenses.setActive(dto.ativa() != null ? dto.ativa() : true);
        expenses.setDueDay(dto.diaVencimento());
        expenses.setInstallmentAmount(installmentValue);
        expenses.setTotalInstallments(dto.totalParcelas());
        return expenses;
    }

    public List<Expenses> findAll() {
        return expensesRepository.findAll();
    }
}
