package com.bcsystems.bonds.service;

import com.bcsystems.bonds.dto.ClienteIneResponse;
import com.bcsystems.bonds.dto.ClienteRequest;
import com.bcsystems.bonds.dto.ClienteResponse;
import com.bcsystems.bonds.dto.ListaNegraRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.util.List;
import java.time.LocalDate;

public interface ClienteService {
    Page<ClienteResponse> listar(String search, Boolean activo, int page, int size);
    Page<ClienteResponse> listarCreditClients(int page, int size);
    Page<ClienteResponse> listarMembresiasActivas(String search, String tipo, LocalDate inicioDesde, LocalDate inicioHasta,
                                                  LocalDate finDesde, LocalDate finHasta, Integer diasMin, Integer diasMax,
                                                  int page, int size);
    ClienteResponse obtenerPorId(Integer id);
    ClienteResponse crear(ClienteRequest request);
    ClienteResponse actualizar(Integer id, ClienteRequest request);
    void eliminar(Integer id);
    List<ClienteResponse> listarEnListaNegra();
    ClienteResponse cambiarListaNegra(Integer id, ListaNegraRequest request);
    ClienteIneResponse subirIne(Integer id, MultipartFile frontal, MultipartFile trasera);
    ClienteIneResponse obtenerIne(Integer id);
    Resource descargarIne(Integer id, String lado);
    ClienteResponse subirFoto(Integer id, MultipartFile foto);
    void eliminarFoto(Integer id);
}
