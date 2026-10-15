package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findTopByEmployeeNoStartingWithOrderByEmployeeNoDesc(String prefix);
    @Query("SELECT c FROM Cliente c WHERE " +
           "(:search IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.apellidoPaterno) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.telefono) LIKE CONCAT('%', :search, '%') " +
           "OR LOWER(c.employeeNo) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.nombre")
    Page<Cliente> buscar(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE " +
           "(:activo IS NULL OR c.activo = :activo) AND " +
           "(:search IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.apellidoPaterno) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.telefono) LIKE CONCAT('%', :search, '%') " +
           "OR LOWER(c.employeeNo) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.nombre")
    Page<Cliente> buscarConActivo(@Param("search") String search, @Param("activo") Boolean activo, Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.activo = true ORDER BY c.nombre")
    Page<Cliente> findActivos(Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.tieneCredito = true AND c.activo = true ORDER BY c.nombre")
    Page<Cliente> findCreditClients(Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.activo = true " +
           "AND c.fechaFinMembresia IS NOT NULL AND c.fechaFinMembresia >= CURRENT_DATE " +
           "AND (:search IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.apellidoPaterno) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.employeeNo) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.tipoMembresia) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.fechaFinMembresia ASC, c.nombre ASC")
    Page<Cliente> findMembresiasActivas(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.enListaNegra = true AND c.activo = true ORDER BY c.fechaListaNegra DESC")
    List<Cliente> findByEnListaNegraTrueOrderByFechaListaNegraDesc();
}
