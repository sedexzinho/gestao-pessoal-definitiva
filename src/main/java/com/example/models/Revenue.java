package com.example.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "revenues")
public class Revenue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reven_id")
    private Long id;

    @Column(name = "reven_ds_name")
    private String name;

    @Column(name = "reven_vl_amount")
    private BigDecimal amount;

    @Column(name = "reven_st_status")
    private String status; // PENDENTE, RECEBIDO

    @Column(name = "reven_ds_type")
    private String type; // AVULSO, FIXO

    @Column(name = "reven_dt_registered_at")
    private LocalDate registeredAt;

    @Column(name = "reven_dt_received_at")
    private LocalDate receivedDate;

    @ManyToOne
    @JoinColumn(name = "catego_id")
    private Category category;

    @Column(name = "reven_fl_active")
    private Boolean active = true;

    @Column(name = "reven_nr_due_day")
    private Integer dueDay;

}
