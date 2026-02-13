package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.models.Parcel;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long>  {
    List<Parcel> findByAtivaTrueAndDiaVencimento(int dia);
}