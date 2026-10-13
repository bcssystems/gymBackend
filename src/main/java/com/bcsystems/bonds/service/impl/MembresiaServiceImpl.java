package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Membresia;
import com.bcsystems.bonds.dto.MembresiaRequest;
import com.bcsystems.bonds.dto.MembresiaResponse;
import com.bcsystems.bonds.dto.RenovarMembresiaRequest;
import com.bcsystems.bonds.dto.VentaResponse;
import com.bcsystems.bonds.domain.Cliente;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.MembresiaRepository;
import com.bcsystems.bonds.repository.ClienteRepository;
import com.bcsystems.bonds.service.MembresiaService;
import com.bcsystems.bonds.service.VentaService;
import com.bcsystems.bonds.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;

import com.bcsystems.bonds.exception.InvalidEntryException;

@Service
@RequiredArgsConstructor
public class MembresiaServiceImpl implements MembresiaService {
    private final MembresiaRepository repository;
    private final ClienteRepository clienteRepository;
    private final VentaService ventaService;
    private final AuditoriaService auditoriaService;

    @Override
    public Page<MembresiaResponse> listar(String search, Boolean activa, Pageable pageable) {
        Page<Membresia> result = activa == null
                ? repository.buscar(search, pageable)
                : repository.findByActiva(activa, pageable);
        return result.map(this::toResponse);
    }

    @Override
    public List<MembresiaResponse> listarActivas() {
        return repository.findByActivaTrueOrderByNombreAsc().stream().map(this::toResponse).toList();
    }

    @Override
    public MembresiaResponse obtenerPorId(Integer id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public MembresiaResponse crear(MembresiaRequest request) {
        if (repository.existsByNombreIgnoreCase(request.nombre())) {
            throw new IllegalArgumentException("Ya existe una membresía con ese nombre");
        }
        Membresia m = new Membresia();
        aplicar(m, request);
        m = repository.save(m);
        auditoriaService.registrar("MEMBRESIA", m.getIdMembresia(), "CREACION", usuarioActual(),
                "Membresía creada: " + m.getNombre());
        return toResponse(m);
    }

    @Override
    @Transactional
    public MembresiaResponse actualizar(Integer id, MembresiaRequest request) {
        Membresia m = find(id);
        aplicar(m, request);
        m = repository.save(m);
        auditoriaService.registrar("MEMBRESIA", m.getIdMembresia(), "ACTUALIZACION", usuarioActual(),
                "Membresía actualizada: " + m.getNombre());
        return toResponse(m);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Membresia m = find(id);
        m.setActiva(false);
        repository.save(m);
        auditoriaService.registrar("MEMBRESIA", id, "ELIMINACION", usuarioActual(),
                "Membresía desactivada: " + m.getNombre());
    }

    @Override
    @Transactional
    public VentaResponse renovar(RenovarMembresiaRequest request) {
        Membresia m = find(request.idMembresia());
        if (!Boolean.TRUE.equals(m.getActiva())) throw new InvalidEntryException("La membresía está inactiva");
        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
        String duracion = request.duracion().trim().toUpperCase();
        double precio;
        switch (duracion) {
            case "DIA" -> precio = m.getPrecioDia();
            case "SEMANA" -> precio = m.getPrecioSemana();
            case "MES" -> precio = m.getPrecioMes();
            case "ANIO" -> precio = m.getPrecioAnio();
            default -> throw new InvalidEntryException("La duración debe ser DIA, SEMANA, MES o ANIO");
        }
        VentaResponse venta = ventaService.ventaRapida(request.idCaja(),
                "Membresía " + m.getNombre() + " - " + duracion, 0.0, precio, 1, cliente.getIdCliente());
        LocalDate hoy = LocalDate.now();
        LocalDate base = cliente.getFechaFinMembresia() != null && !cliente.getFechaFinMembresia().isBefore(hoy)
                ? cliente.getFechaFinMembresia() : hoy;
        LocalDate inicio = cliente.getFechaInicioMembresia() != null && !cliente.getFechaFinMembresia().isBefore(hoy)
                ? cliente.getFechaInicioMembresia() : hoy;
        LocalDate fin = switch (duracion) {
            case "DIA" -> base.plusDays(1);
            case "SEMANA" -> base.plusWeeks(1);
            case "MES" -> base.plusMonths(1);
            default -> base.plusYears(1);
        };
        cliente.setFechaInicioMembresia(inicio);
        cliente.setFechaFinMembresia(fin);
        cliente.setTipoMembresia(m.getNombre());
        cliente.setActivo(true);
        clienteRepository.save(cliente);
        auditoriaService.registrar("MEMBRESIA", m.getIdMembresia(), "ACTUALIZACION",
                usuarioActual(), "Renovación de " + m.getNombre() + " para cliente #" + cliente.getIdCliente()
                        + " hasta " + fin);
        return venta;
    }

    private String usuarioActual() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "sistema";
    }

    private void aplicar(Membresia m, MembresiaRequest r) {
        m.setNombre(r.nombre());
        m.setDescripcion(r.descripcion());
        m.setPrecioDia(r.precioDia());
        m.setPrecioSemana(r.precioSemana());
        m.setPrecioMes(r.precioMes());
        m.setPrecioAnio(r.precioAnio());
        m.setActiva(r.activa() == null || r.activa());
    }

    private Membresia find(Integer id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Membresía no encontrada"));
    }

    private MembresiaResponse toResponse(Membresia m) {
        return new MembresiaResponse(m.getIdMembresia(), m.getNombre(), m.getDescripcion(),
                m.getPrecioDia(), m.getPrecioSemana(), m.getPrecioMes(), m.getPrecioAnio(),
                m.getActiva(), m.getFechaCreacion());
    }
}
