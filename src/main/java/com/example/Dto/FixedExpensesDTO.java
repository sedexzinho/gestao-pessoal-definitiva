package com.example.Dto;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FixedExpensesDTO(
        @NotBlank(message = "Nome não pode ser vazio") 
        String nome,

        @NotNull(message = "O valor é obrigatório") 
        @Positive(message = "O valor deve ser positivo") 
        BigDecimal valor,

    

        @NotNull(message = "O dia de vencimento é obrigatório") 
        @Range(min = 1, max = 31, message = "Dia de vencimento inválido") 
        Integer dia_vencimento,

        String nomeCategoria
    )

{
}
