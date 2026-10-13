package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Membresia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MembresiaRepository extends JpaRepository<Membresia, Integer> {
    boolean existsByNombreIgnoreCase(String nombre);
    List<Membresia> findByActivaTrueOrderByNombreAsc();
    Page<Membresia> findByActiva(Boolean activa, Pageable pageable);
    @Query("SELECT m FROM Membresia m WHERE (:search IS NULL OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY m.nombre ASC")
    Page<Membresia> buscar(String search, Pageable pageable);
}
