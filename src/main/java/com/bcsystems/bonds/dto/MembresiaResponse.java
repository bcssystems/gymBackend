package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;

public record MembresiaResponse(
        Integer idMembresia,
        String nombre,
        String descripcion,
        Double precioDia,
        Double precioSemana,
        Double precioMes,
        Double precioAnio,
        Boolean activa,
        LocalDateTime fechaCreacion
) {}
