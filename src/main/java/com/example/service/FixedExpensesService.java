package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Dto.FixedExpensesDTO;
import com.example.models.Category;
import com.example.models.FixedExpenses;
import com.example.repository.CategoryRepository;
import com.example.repository.FixedExpensesRepository;

@Service
public class FixedExpensesService {
    @Autowired
    private final FixedExpensesRepository fixedExpensesRepository;
     @Autowired
    private CategoryRepository categoryRepository;

    public FixedExpensesService(FixedExpensesRepository fixedExpensesRepository) {
        this.fixedExpensesRepository = fixedExpensesRepository;
    }

    public void creatNewFixedExpenses(FixedExpensesDTO dto) {
        Category category = categoryRepository.findByNomeIgnoreCase(dto.nomeCategoria()).orElseGet(() -> {
            Category nova = new Category();
            nova.setNome(dto.nomeCategoria());
            return categoryRepository.save(nova);
        });
        FixedExpenses fixedExpenses = new FixedExpenses();
        fixedExpenses.setNome(dto.nome());
        fixedExpenses.setValor(dto.valor());
        fixedExpenses.setCategoria(category);
        fixedExpenses.setDiaVencimento(dto.dia_vencimento());
        fixedExpensesRepository.save(fixedExpenses);

    }
}
