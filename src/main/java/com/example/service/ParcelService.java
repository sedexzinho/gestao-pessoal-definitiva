package com.example.service;

import java.math.BigDecimal;

import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.example.Dto.ExpensesDTO;
import com.example.models.Category;

import com.example.models.Parcel;

import com.example.repository.ParcelRepository;

import jakarta.transaction.Transactional;

@Service

public class ParcelService {

    private final ParcelRepository parcelRepository;

    public ParcelService(ParcelRepository parcelRepository) {
        this.parcelRepository = parcelRepository;
    }

    @Transactional
    public void criarNovoParcelamento(ExpensesDTO dto, Category category) {
        // 1. Cálculo do valor de cada parcela
        // No mercado, usamos RoundingMode.HALF_EVEN para evitar erros de centavos
        BigDecimal valorDaParcela = dto.valorPago().divide(
                BigDecimal.valueOf(dto.qtdParcelas()), 2, RoundingMode.HALF_EVEN);

        // 2. Mapeamento Manual do DTO para a Entidade Parcela
        Parcel parcel = new Parcel();
        parcel.setNome(dto.nome());
        parcel.setValor(valorDaParcela); // Aqui guardamos o valor UNITÁRIO
        parcel.setDiaVencimento(dto.diaVencimento());
        parcel.setTotalParcelas(dto.qtdParcelas());
        parcel.setParcelasRestantes(dto.qtdParcelas());
        parcel.setParcelaAtual(0); 
        parcel.setCategoria(category);
        parcel.setAtiva(true);

        // 3. Persistência do "Contrato"
        parcelRepository.save(parcel);

        // Dica de Mentor: No futuro, se quiser que a 1ª parcela seja paga no ato,
        // você chamaria o método de efetivação aqui mesmo.
    }

}