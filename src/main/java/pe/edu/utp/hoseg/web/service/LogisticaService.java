package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.LineaLogisticaVista;
import pe.edu.utp.hoseg.web.dto.PedidoLogisticaVista;
import pe.edu.utp.hoseg.web.model.Envio;
import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.repository.EnvioRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LogisticaService {

    private final EnvioRepository envioRepository;

    public LogisticaService(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    /** Lista pedidos pagados filtrados por modalidad (DELIVERY o RECOJO_TIENDA). */
    public List<PedidoLogisticaVista> pedidosPagadosPorTipo(TipoEnvio tipo) {
        return envioRepository.findByVentaEstadoOrderByCreadoEnDesc(EstadoVenta.PAGADO).stream()
                .filter(e -> e.getTipo() == tipo)
                .map(LogisticaService::aVista)
                .toList();
    }

    /** Avanza el estado del envío respetando las transiciones permitidas. */
    @Transactional
    public void avanzarEstado(Integer idEnvio, EstadoEnvio nuevoEstado, String courier, String codigoSeguimiento) {
        Envio envio = envioRepository.findById(idEnvio)
                .orElseThrow(() -> new RecursoNoEncontradoException("Envío no encontrado: " + idEnvio));

        if (envio.getVenta().getEstado() != EstadoVenta.PAGADO) {
            throw new NegocioException("Solo se pueden atender pedidos con pago confirmado.");
        }

        EstadoEnvio actual = envio.getEstado();
        boolean esDelivery = envio.getTipo() == TipoEnvio.DELIVERY;

        if (nuevoEstado == EstadoEnvio.EN_CAMINO) {
            if (!esDelivery || actual != EstadoEnvio.PREPARANDO) {
                throw new NegocioException("Transición no permitida hacia En Camino.");
            }
            envio.setEstado(EstadoEnvio.EN_CAMINO);
            envio.setCourier(limpio(courier) != null ? limpio(courier) : "HÖSÉG Express");
            envio.setCodigoSeguimiento(limpio(codigoSeguimiento));
            envio.setFechaDespacho(OffsetDateTime.now());
        } else if (nuevoEstado == EstadoEnvio.ENTREGADO) {
            boolean valido = (esDelivery && actual == EstadoEnvio.EN_CAMINO)
                    || (!esDelivery && actual == EstadoEnvio.PREPARANDO);
            if (!valido) {
                throw new NegocioException("Transición no permitida hacia Entregado.");
            }
            envio.setEstado(EstadoEnvio.ENTREGADO);
            envio.setFechaEntrega(OffsetDateTime.now());
        } else {
            throw new NegocioException("Estado de destino no válido para logística.");
        }

        envioRepository.save(envio);
    }

    private static PedidoLogisticaVista aVista(Envio e) {
        List<LineaLogisticaVista> lineas = e.getVenta().getDetalles().stream()
                .map(d -> new LineaLogisticaVista(
                        d.getProducto().getCodigo(),
                        d.getProducto().getNombre(),
                        d.getProducto().getTallaMostrada(),
                        d.getCantidad()))
                .toList();

        String destino = e.getTipo() == TipoEnvio.DELIVERY
                ? String.join(", ",
                    java.util.stream.Stream.of(e.getDireccion(), e.getDistrito(), e.getProvincia())
                            .filter(s -> s != null && !s.isBlank()).toList())
                : "Recojo en tienda HÖSÉG";

        return new PedidoLogisticaVista(
                e.getId(),
                e.getVenta().getId(),
                e.getVenta().getCodigoComprobante(),
                e.getVenta().getFecha(),
                e.getTipo(),
                e.getEstado(),
                e.getNombreReceptor(),
                e.getTelefonoReceptor(),
                destino,
                e.getCourier(),
                e.getCodigoSeguimiento(),
                lineas
        );
    }

    private static String limpio(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
