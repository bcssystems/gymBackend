package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RenovarMembresiaRequest(
        @NotNull Integer idCaja,
        @NotNull Integer idCliente,
        @NotNull Integer idMembresia,
        @NotBlank String duracion
) {}
