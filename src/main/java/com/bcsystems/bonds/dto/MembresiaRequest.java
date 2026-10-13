package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MembresiaRequest(
        @NotBlank String nombre,
        String descripcion,
        @NotNull @PositiveOrZero Double precioDia,
        @NotNull @PositiveOrZero Double precioSemana,
        @NotNull @PositiveOrZero Double precioMes,
        @NotNull @PositiveOrZero Double precioAnio,
        Boolean activa
) {}
