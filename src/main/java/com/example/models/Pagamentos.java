package com.example.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pagamentos")
public class Pagamentos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;
@Column(name = "tipo_despesa", nullable = false)
    private String tipo;


    @ManyToOne
    @JoinColumn(name = "id_despesa", nullable = false)
    private Expenses despesa;

    // Data em que o pagamento foi feito
    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    // Valor pago nesta parcela
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    // Status do pagamento: PENDENTE, PAGO, ATRASADO
    @Column(nullable = false)
    private String status;

    // Número da parcela (ex: parcela 3 de 10)
    @Column(name = "numero_parcela")
    private Integer numeroParcela;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Category categoria;
}
