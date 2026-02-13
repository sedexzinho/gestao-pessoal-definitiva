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
public class Parcel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "dia_vencimento")
    private Integer diaVencimento;

    @Column(name = "parcelas_restantes")
    private Integer parcelasRestantes;

    @Column(name = "parcela_atual")
    private Integer parcelaAtual;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;
    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Category categoria;

    private boolean ativa = true;

}
