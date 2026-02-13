package com.example.service;

import java.math.BigDecimal;

import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.example.models.Expenses;
import com.example.models.User;
import com.example.repository.ExpensesRepository;
import com.example.repository.UserRepository;
@Service
public class ExpensesService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ExpensesRepository expensesRepository;
    
    @Autowired
    @Lazy
    private ParcelService parcelaService;
    
      public BigDecimal consultarSaldoSimples() {
        // Busca o seu salário (ID 1)
        BigDecimal salario = userRepository.findById(1L)
                .map(User::getSalarioMensal)
                .orElse(BigDecimal.ZERO);

        // Soma todas as despesas
        BigDecimal totalGasto = expensesRepository.findAll().stream()
                .map(Expenses::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return salario.subtract(totalGasto);
    }
}
