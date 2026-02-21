package com.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.example.Dto.RevenueDTO;
import com.example.models.Category;
import com.example.models.Revenue;
import com.example.models.User;
import com.example.repository.CategoryRepository;
import com.example.repository.RevenuesRepository;
import com.example.repository.UserRepository;

@Service
public class RevenueService {

    private UserRepository userRepository;
    private RevenuesRepository revenueRepository;
    private CategoryRepository categoryRepository;

    public RevenueService(
            UserRepository userRepository,
            RevenuesRepository revenueRepository,
            CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.revenueRepository = revenueRepository;
        this.categoryRepository = categoryRepository;
    }

    public void registrarReceitas(RevenueDTO dto) {
        Category category = categoryRepository.findByNameIgnoreCase(dto.nomeCategoria())
                .orElseThrow(() -> new RuntimeException(
                        "Categoria não encontrada. Crie a categoria primeiro em Despesas ou na tela de Categorias."));

        Revenue revenue = mapToEntity(dto, category);
        revenueRepository.save(revenue);
        if ("AVULSO".equals(dto.tipo())) {
            adicionarAoSaldo(revenue.getAmount());

        } else if ("FIXO".equals(dto.tipo()) && dto.dataRecebimento() != null) {
            adicionarAoSaldo(revenue.getAmount());
        }
    }

    public void adicionarAoSaldo(BigDecimal value) {
        User usuario = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        BigDecimal currentSalary = usuario.getMonthlySalary();
        usuario.setMonthlySalary(currentSalary.add(value));
        userRepository.save(usuario);
    }

    private Revenue mapToEntity(RevenueDTO dto, Category category) {
        Revenue revenue = new Revenue();
        revenue.setName(dto.nome());
        revenue.setAmount(dto.valor());
        revenue.setCategory(category);
        revenue.setRegisteredAt(LocalDate.now());
        revenue.setType(dto.tipo());
        revenue.setActive(dto.ativa() != null ? dto.ativa() : true);
        revenue.setDueDay(dto.diaVencimento());

        LocalDate dataRecebimento = dto.dataRecebimento();
        LocalDate hoje = LocalDate.now();

        if ("AVULSO".equals(dto.tipo())) {
            revenue.setStatus(("RECEBIDO"));
            revenue.setReceivedDate(hoje);

        } else if ("FIXO".equals(dto.tipo())) {
            if (dataRecebimento != null && !dataRecebimento.isAfter(hoje)) {
                revenue.setStatus("RECEBIDO");
                revenue.setReceivedDate(dataRecebimento);
            } else {
                revenue.setStatus("PENDENTE");
            }
        }

        return revenue;
    }

}