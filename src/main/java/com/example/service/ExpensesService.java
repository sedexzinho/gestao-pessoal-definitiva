
package com.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Dto.ExpensesDTO;
import com.example.models.Category;
import com.example.models.Expenses;
import com.example.models.Parcel;
import com.example.repository.CategoryRepository;
import com.example.repository.ExpensesRepository;
import com.example.repository.UserRepository;

@Service
public class ExpensesService {

    private UserRepository userRepository;

    private ExpensesRepository expensesRepository;

    private CategoryRepository categoryRepository;

    @Lazy
    private ParcelService parcelService;

    public ExpensesService(
            UserRepository userRepository, 
            ExpensesRepository expensesRepository, 
            CategoryRepository categoryRepository, 
            @Lazy ParcelService parcelService) { // Mantemos o @Lazy aqui para evitar dependência circular
        this.userRepository = userRepository;
        this.expensesRepository = expensesRepository;
        this.categoryRepository = categoryRepository;
        this.parcelService = parcelService;
    }

   public BigDecimal consultarSaldoSimples() {
    // 1. Se já é BigDecimal, o map apenas "deixa passar" o valor
    BigDecimal salario = userRepository.findById(1L)
            .map(u -> u.getSalarioMensal()) 
            .orElse(BigDecimal.ZERO);

    // 2. O total gasto (Stream de Expenses)
    BigDecimal totalGasto = expensesRepository.findAll().stream()
            .map(Expenses::getValorPago)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 3. Agora a conta fecha perfeitamente
    return salario.subtract(totalGasto);
}

    @Transactional
    public void registrarGasto(ExpensesDTO dto) {

        Category category = categoryRepository.findByNomeIgnoreCase(dto.nomeCategoria()).orElseGet(() -> {

            Category nova = new Category();
            nova.setNome(dto.nomeCategoria());
            return categoryRepository.save(nova);
        });

        if (dto.qtdParcelas() != null && dto.qtdParcelas() > 1) {
            parcelService.criarNovoParcelamento(dto, category);
        } else {
            Expenses expenses = mapToEntity(dto, category);
            expensesRepository.save(expenses);
        }

    }

    private Expenses mapToEntity(ExpensesDTO dto, Category category) {
        Expenses expenses = new Expenses();
        expenses.setNome(dto.nome());
        expenses.setValorPago(dto.valorPago());
        expenses.setCategoria(category);
        expenses.setDataRegistro(LocalDate.now());
        expenses.setStatus("PAGO");
        return expenses;
    }

    @Transactional
    public void efetivarPagamento(Parcel parcel) {
        BigDecimal valorParaCobrar;
        int parcelaAtualSendoPAga = parcel.getParcelaAtual() + 1;

        if (parcel.getParcelasRestantes() == 1) {
            BigDecimal valorPago = parcel.getValor().multiply(BigDecimal.valueOf(parcel.getParcelaAtual()));
            valorParaCobrar = parcel.getValorTotal().subtract(valorPago);

        } else {
            valorParaCobrar = parcel.getValor();
        }
    Expenses newExpenses = new Expenses();
        newExpenses.setNome(parcel.getNome() + "(" + parcelaAtualSendoPAga + "/" + parcel.getTotalParcelas() + ")");
        newExpenses.setValorPago(valorParaCobrar);
        newExpenses.setCategoria(parcel.getCategoria());
        newExpenses.setDataRegistro(LocalDate.now());
        newExpenses.setStatus("PAGO");
        newExpenses.setParcela(parcel);
        expensesRepository.save(newExpenses);
        parcel.setParcelaAtual(parcelaAtualSendoPAga);
        parcel.setParcelasRestantes(parcel.getParcelasRestantes() - 1);
        if (parcel.getParcelasRestantes() <= 0) {
            parcel.setAtiva(false);
        }
    }

    public List<Expenses> findAll() {
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

}
