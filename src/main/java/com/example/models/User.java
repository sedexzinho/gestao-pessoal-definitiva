package com.example.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Long id;

    @Column(name = "users_cd")
    private Long code;

    @Column(name = "users_ds_name")
    private String name;

    @Column(name = "users_vl_monthly_salary")
    private BigDecimal monthlySalary;

    @Column(name = "users_dt_created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
