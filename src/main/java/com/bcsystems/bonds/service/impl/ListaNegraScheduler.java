package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Cliente;
import com.bcsystems.bonds.domain.Credito;
import com.bcsystems.bonds.domain.en.EstadoCredito;
import com.bcsystems.bonds.repository.AbonoRepository;
import com.bcsystems.bonds.repository.ClienteRepository;
import com.bcsystems.bonds.repository.CreditoRepository;
import com.bcsystems.bonds.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListaNegraScheduler {

    private final ClienteRepository clienteRepository;
    private final CreditoRepository creditoRepository;
    private final AbonoRepository abonoRepository;
    private final ConfiguracionService configuracionService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void ejecutarRevisionAutomatica() {
        int diasLimite = configuracionService.getValorInt("diasDesactivacionMembresia", 0);
        LocalDate hoy = LocalDate.now();

        List<Cliente> candidatos = clienteRepository.findActivos(null).stream()
                .filter(c -> c.getFechaFinMembresia() != null)
                .toList();

        int agregados = 0;
        for (Cliente cliente : candidatos) {
            if (cliente.getFechaFinMembresia().plusDays(diasLimite).isBefore(hoy)) {
                cliente.setActivo(false);
                clienteRepository.save(cliente);
                agregados++;
                log.info("Cliente desactivado por membresía vencida: {} {}", cliente.getNombre(), cliente.getApellidoPaterno());
            }
        }
        if (agregados > 0) {
            log.info("Lista negra automatica: {} clientes marcados", agregados);
        }
    }

}
