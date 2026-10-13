package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.MembresiaRequest;
import com.bcsystems.bonds.dto.MembresiaResponse;
import com.bcsystems.bonds.dto.RenovarMembresiaRequest;
import com.bcsystems.bonds.dto.VentaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MembresiaService {
    Page<MembresiaResponse> listar(String search, Boolean activa, Pageable pageable);
    List<MembresiaResponse> listarActivas();
    MembresiaResponse obtenerPorId(Integer id);
    MembresiaResponse crear(MembresiaRequest request);
    MembresiaResponse actualizar(Integer id, MembresiaRequest request);
    void eliminar(Integer id);
    VentaResponse renovar(RenovarMembresiaRequest request);
}
