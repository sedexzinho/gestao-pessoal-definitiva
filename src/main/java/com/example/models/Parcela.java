package com.example.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Data

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parcela")
public class Parcela {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_parcela;

    @Column(name = "nome")
    private String nome;

    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "dia_vencimento")
    private int diaVencimento;

    @Column(name = "parcelas_restantes")
    private int parcelasRestantes;

    @Column(name = "parcela_atual")
    private int parcelaAtual;

    @Column(name = "total_parcelas")
    private int totalParcelas;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    private boolean ativa = true;

}
