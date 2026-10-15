package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Cliente;
import com.bcsystems.bonds.domain.ClienteIne;
import com.bcsystems.bonds.dto.ClienteIneResponse;
import com.bcsystems.bonds.dto.ClienteRequest;
import com.bcsystems.bonds.dto.ClienteResponse;
import com.bcsystems.bonds.dto.ListaNegraRequest;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.ClienteIneRepository;
import com.bcsystems.bonds.repository.ClienteRepository;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteIneRepository clienteIneRepository;
    private final AuditoriaService auditoriaService;

    @Value("${app.upload.dir:./uploads/multimedia}")
    private String uploadDir;

    @PostConstruct
    @Transactional
    void asignarEmployeeNosExistentes() {
        clienteRepository.findAll().stream()
                .filter(cliente -> cliente.getEmployeeNo() == null || cliente.getEmployeeNo().isBlank())
                .forEach(cliente -> {
                    cliente.setEmployeeNo(siguienteEmployeeNo());
                    clienteRepository.save(cliente);
                });
    }

    @Override
    public Page<ClienteResponse> listar(String search, Boolean activo, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("nombre"));
        Page<Cliente> clientes;
        if (activo == null) {
            if (search != null && !search.isBlank()) {
                clientes = clienteRepository.buscar(search, pageable);
            } else {
                clientes = clienteRepository.findActivos(pageable);
            }
        } else {
            clientes = clienteRepository.buscarConActivo(search, activo, pageable);
        }
        return clientes.map(this::toResponse);
    }

    @Override
    public Page<ClienteResponse> listarCreditClients(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("nombre"));
        return clienteRepository.findCreditClients(pageable).map(this::toResponse);
    }

    @Override
    public Page<ClienteResponse> listarMembresiasActivas(String search, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return clienteRepository.findMembresiasActivas(
                search == null || search.isBlank() ? null : search.trim(), pageable).map(this::toResponse);
    }

    @Override
    public ClienteResponse obtenerPorId(Integer id) {
        return toResponse(buscarOExcepcion(id));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        Cliente cliente = Cliente.builder()
                .nombre(request.nombre())
                .apellidoPaterno(request.apellidoPaterno())
                .apellidoMaterno(request.apellidoMaterno())
                .telefono(request.telefono() != null ? request.telefono() : "")
                .codigoPais(request.codigoPais() != null ? request.codigoPais() : "+52")
                .whatsapp(request.whatsapp())
                .empresa(request.empresa())
                .regimenFiscal(request.regimenFiscal() != null ? request.regimenFiscal() : "N/A")
                .cp(request.cp())
                .direccion(request.direccion())
                .calle(request.calle())
                .numExt(request.numExt())
                .numInt(request.numInt())
                .colonia(request.colonia())
                .municipio(request.municipio())
                .estado(request.estado())
                .rfc(request.rfc())
                .representanteLegal(request.representanteLegal())
                .direccionEntrega(request.direccionEntrega())
                .tieneCredito(request.tieneCredito() != null && request.tieneCredito())
                .limiteCredito(request.limiteCredito())
                .saldoActual(0.0)
                .enListaNegra(false)
                .employeeNo(siguienteEmployeeNo())
                .fechaInicioMembresia(null)
                .fechaFinMembresia(null)
                .tipoMembresia(null)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();
        cliente = clienteRepository.save(cliente);

        auditoriaService.registrar("Cliente", cliente.getIdCliente(), "CREACION",
                obtenerUsuarioActual(), "Cliente creado: " + cliente.getNombre());
        return toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {
        Cliente cliente = buscarOExcepcion(id);
        cliente.setNombre(request.nombre());
        cliente.setApellidoPaterno(request.apellidoPaterno());
        cliente.setApellidoMaterno(request.apellidoMaterno());
        cliente.setTelefono(request.telefono() != null ? request.telefono() : "");
        cliente.setCodigoPais(request.codigoPais() != null ? request.codigoPais() : "+52");
        cliente.setWhatsapp(request.whatsapp());
        cliente.setEmpresa(request.empresa());
        cliente.setRegimenFiscal(request.regimenFiscal() != null ? request.regimenFiscal() : "N/A");
        cliente.setCp(request.cp());
        cliente.setDireccion(request.direccion());
        cliente.setCalle(request.calle());
        cliente.setNumExt(request.numExt());
        cliente.setNumInt(request.numInt());
        cliente.setColonia(request.colonia());
        cliente.setMunicipio(request.municipio());
        cliente.setEstado(request.estado());
        cliente.setRfc(request.rfc());
        cliente.setRepresentanteLegal(request.representanteLegal());
        cliente.setDireccionEntrega(request.direccionEntrega());
        cliente.setTieneCredito(request.tieneCredito() != null && request.tieneCredito());
        cliente.setLimiteCredito(request.limiteCredito());
        if (cliente.getEmployeeNo() == null || cliente.getEmployeeNo().isBlank()) {
            cliente.setEmployeeNo(siguienteEmployeeNo());
        }
        cliente = clienteRepository.save(cliente);

        auditoriaService.registrar("Cliente", id, "ACTUALIZACION",
                obtenerUsuarioActual(), "Cliente actualizado: " + cliente.getNombre());
        return toResponse(cliente);
    }

    private String vacioANulo(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String siguienteEmployeeNo() {
        String prefijo = "GYM";
        int siguiente = clienteRepository.findTopByEmployeeNoStartingWithOrderByEmployeeNoDesc(prefijo)
                .map(Cliente::getEmployeeNo)
                .map(value -> {
                    try {
                        return Integer.parseInt(value.substring(prefijo.length())) + 1;
                    } catch (RuntimeException ex) {
                        return 1;
                    }
                })
                .orElse(1);
        return prefijo + String.format("%06d", siguiente);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Cliente cliente = buscarOExcepcion(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Override
    public List<ClienteResponse> listarEnListaNegra() {
        return clienteRepository.findByEnListaNegraTrueOrderByFechaListaNegraDesc().stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ClienteResponse cambiarListaNegra(Integer id, ListaNegraRequest request) {
        Cliente cliente = buscarOExcepcion(id);
        if (Boolean.TRUE.equals(request.enListaNegra()) && Boolean.TRUE.equals(cliente.getEnListaNegra())) {
            throw new InvalidEntryException("El cliente ya está en lista negra");
        }
        if (!Boolean.TRUE.equals(request.enListaNegra()) && !Boolean.TRUE.equals(cliente.getEnListaNegra())) {
            throw new InvalidEntryException("El cliente no está en lista negra");
        }
        cliente.setEnListaNegra(request.enListaNegra());
        if (Boolean.TRUE.equals(request.enListaNegra())) {
            cliente.setFechaListaNegra(LocalDateTime.now());
            cliente.setMotivoListaNegra(request.motivo());
        } else {
            cliente.setFechaListaNegra(null);
            cliente.setMotivoListaNegra(null);
        }
        cliente = clienteRepository.save(cliente);

        auditoriaService.registrar("Cliente", id, "ACTUALIZACION",
                obtenerUsuarioActual(),
                Boolean.TRUE.equals(request.enListaNegra())
                        ? "Cliente agregado a lista negra: " + request.motivo()
                        : "Cliente removido de lista negra");
        return toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse subirFoto(Integer id, MultipartFile foto) {
        Cliente cliente = buscarOExcepcion(id);
        if (foto == null || foto.isEmpty()) {
            throw new InvalidEntryException("Debe proporcionar una foto");
        }
        String contentType = foto.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new InvalidEntryException("El archivo debe ser una imagen");
        }
        if (foto.getSize() > 5 * 1024 * 1024) {
            throw new InvalidEntryException("La foto no puede superar 5 MB");
        }
        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            borrarArchivo(cliente.getFotoUrl(), uploadPath);

            String extension = extensionImagen(foto.getOriginalFilename(), contentType);
            String nombre = UUID.randomUUID() + ".cliente" + extension;
            Files.copy(foto.getInputStream(), uploadPath.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
            cliente.setFotoUrl("/uploads/" + nombre);
            cliente = clienteRepository.save(cliente);
            auditoriaService.registrar("Cliente", id, "ACTUALIZACION", obtenerUsuarioActual(), "Foto de cliente actualizada");
            return toResponse(cliente);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la foto del cliente: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void eliminarFoto(Integer id) {
        Cliente cliente = buscarOExcepcion(id);
        try {
            borrarArchivo(cliente.getFotoUrl(), Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar la foto del cliente: " + e.getMessage());
        }
        cliente.setFotoUrl(null);
        clienteRepository.save(cliente);
        auditoriaService.registrar("Cliente", id, "ACTUALIZACION", obtenerUsuarioActual(), "Foto de cliente eliminada");
    }

    private String extensionImagen(String original, String contentType) {
        if (original != null && original.lastIndexOf('.') >= 0) {
            String ext = original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (ext.matches("\\.(jpg|jpeg|png|webp|gif)")) return ext;
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

    @Override
    @Transactional
    public ClienteIneResponse subirIne(Integer id, MultipartFile frontal, MultipartFile trasera) {
        Cliente cliente = buscarOExcepcion(id);
        if (frontal == null && trasera == null) {
            throw new InvalidEntryException("Debe proporcionar al menos una foto del INE");
        }
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            ClienteIne ine = clienteIneRepository.findByClienteIdCliente(id)
                    .orElse(ClienteIne.builder().cliente(cliente).subidoEn(LocalDateTime.now()).build());

            if (frontal != null && !frontal.isEmpty()) {
                borrarArchivo(ine.getUrlFotoFrontal(), uploadPath);
                String nombre = UUID.randomUUID() + ".ine.f.jpg";
                Files.copy(frontal.getInputStream(), uploadPath.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
                ine.setUrlFotoFrontal("/uploads/" + nombre);
                ine.setNombreArchivoFrontal(frontal.getOriginalFilename());
            }
            if (trasera != null && !trasera.isEmpty()) {
                borrarArchivo(ine.getUrlFotoTrasera(), uploadPath);
                String nombre = UUID.randomUUID() + ".ine.t.jpg";
                Files.copy(trasera.getInputStream(), uploadPath.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
                ine.setUrlFotoTrasera("/uploads/" + nombre);
                ine.setNombreArchivoTrasera(trasera.getOriginalFilename());
            }
            ine.setSubidoEn(LocalDateTime.now());
            ine = clienteIneRepository.save(ine);

            auditoriaService.registrar("ClienteIne", id, "CREACION",
                    obtenerUsuarioActual(), "INE subido para: " + cliente.getNombre());
            return toIneResponse(ine);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar las fotos del INE: " + e.getMessage());
        }
    }

    @Override
    public ClienteIneResponse obtenerIne(Integer id) {
        return clienteIneRepository.findByClienteIdCliente(id)
                .map(this::toIneResponse)
                .orElse(null);
    }

    @Override
    public Resource descargarIne(Integer id, String lado) {
        ClienteIne ine = clienteIneRepository.findByClienteIdCliente(id)
                .orElseThrow(() -> new NotFoundException("INE no encontrada"));
        String url = "frontal".equalsIgnoreCase(lado) ? ine.getUrlFotoFrontal() : ine.getUrlFotoTrasera();
        if (url == null || !url.startsWith("/uploads/")) throw new NotFoundException("Foto del INE no encontrada");
        Resource recurso = new FileSystemResource(Paths.get(uploadDir, url.substring("/uploads/".length())));
        if (!recurso.exists()) throw new NotFoundException("Archivo del INE no encontrado");
        return recurso;
    }

    private void borrarArchivo(String url, Path dir) throws IOException {
        if (url != null && url.startsWith("/uploads/")) {
            Files.deleteIfExists(dir.resolve(url.substring("/uploads/".length())));
        }
    }

    private Cliente buscarOExcepcion(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado con id: " + id));
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }

    private ClienteIneResponse toIneResponse(ClienteIne ine) {
        return new ClienteIneResponse(
                ine.getIdClienteIne(), ine.getCliente().getIdCliente(),
                ine.getUrlFotoFrontal(), ine.getUrlFotoTrasera(),
                ine.getNombreArchivoFrontal(), ine.getNombreArchivoTrasera(),
                ine.getSubidoEn());
    }

    private ClienteResponse toResponse(Cliente c) {
        boolean tieneIne = clienteIneRepository.findByClienteIdCliente(c.getIdCliente()).isPresent();
        return new ClienteResponse(
                c.getIdCliente(), c.getNombre(),
                c.getApellidoPaterno(), c.getApellidoMaterno(),
                c.getTelefono(), c.getCodigoPais(),
                c.getWhatsapp(), c.getEmpresa(),
                c.getRegimenFiscal(), c.getCp(), c.getDireccion(),
                c.getCalle(), c.getNumExt(), c.getNumInt(), c.getColonia(),
                c.getMunicipio(), c.getEstado(),
                c.getRfc(), c.getRepresentanteLegal(), c.getDireccionEntrega(),
                c.getActivo(), c.getFechaRegistro(),
                c.getTieneCredito(), c.getLimiteCredito(), c.getSaldoActual(),
                c.getEnListaNegra(), c.getFechaListaNegra(), c.getMotivoListaNegra(),
                tieneIne, c.getEmployeeNo(), c.getFechaInicioMembresia(),
                c.getFechaFinMembresia(), c.getTipoMembresia(), c.getFotoUrl());
    }
}
