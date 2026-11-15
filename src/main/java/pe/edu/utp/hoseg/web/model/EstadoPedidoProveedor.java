package pe.edu.utp.hoseg.web.model;

public enum EstadoPedidoProveedor {
    PENDIENTE("Pendiente"),
    RECIBIDO("Recibido"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedidoProveedor(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
