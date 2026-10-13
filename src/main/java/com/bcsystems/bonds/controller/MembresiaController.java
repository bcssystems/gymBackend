package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.dto.MembresiaRequest;
import com.bcsystems.bonds.dto.MembresiaResponse;
import com.bcsystems.bonds.dto.RenovarMembresiaRequest;
import com.bcsystems.bonds.dto.VentaResponse;
import com.bcsystems.bonds.service.MembresiaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/membresias")
public class MembresiaController {
    private final MembresiaService service;

    public MembresiaController(MembresiaService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasAuthority('MEMBRESIAS_VER')")
    public ResponseEntity<Page<MembresiaResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activa,
            @PageableDefault(size = 50, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(service.listar(search, activa, pageable));
    }

    @GetMapping("/activas")
    @PreAuthorize("hasAuthority('MEMBRESIAS_VER') or hasAuthority('VENTAS_VER')")
    public ResponseEntity<List<MembresiaResponse>> listarActivas() { return ResponseEntity.ok(service.listarActivas()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MEMBRESIAS_VER')")
    public ResponseEntity<MembresiaResponse> obtener(@PathVariable Integer id) { return ResponseEntity.ok(service.obtenerPorId(id)); }

    @PostMapping
    @PreAuthorize("hasAuthority('MEMBRESIAS_CREAR')")
    public ResponseEntity<MembresiaResponse> crear(@Valid @RequestBody MembresiaRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MEMBRESIAS_EDITAR')")
    public ResponseEntity<MembresiaResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody MembresiaRequest request) { return ResponseEntity.ok(service.actualizar(id, request)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MEMBRESIAS_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) { service.eliminar(id); return ResponseEntity.noContent().build(); }

    @PostMapping("/renovar")
    @PreAuthorize("hasAuthority('VENTAS_CREAR')")
    public ResponseEntity<VentaResponse> renovar(@Valid @RequestBody RenovarMembresiaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.renovar(request));
    }
}
