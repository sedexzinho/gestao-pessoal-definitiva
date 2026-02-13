package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.models.Parcel;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long>  {
    
}