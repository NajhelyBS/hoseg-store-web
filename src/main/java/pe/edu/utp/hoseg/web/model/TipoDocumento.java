package pe.edu.utp.hoseg.web.model;

import java.util.regex.Pattern;

/**
 * Tipos de documento de cliente.tipo_doc y libro_reclamaciones.tipo_doc, con
 * el mismo formato que exige cliente_documento_check (07_normalizacion.sql).
 */
public enum TipoDocumento {
    DNI("DNI", "^[0-9]{8}$"),
    CE("Carné de extranjería", "^[A-Za-z0-9]{9,12}$"),
    PASAPORTE("Pasaporte", "^[A-Za-z0-9]{6,12}$"),
    RUC("RUC", "^[0-9]{11}$");

    private final String etiqueta;
    private final Pattern formato;

    TipoDocumento(String etiqueta, String formato) {
        this.etiqueta = etiqueta;
        this.formato = Pattern.compile(formato);
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * true si el número tiene el formato que acepta la base para este tipo.
     * Para el RUC además se comprueba el prefijo y el dígito verificador.
     */
    public boolean admite(String numero) {
        if (numero == null || !formato.matcher(numero).matches()) {
            return false;
        }
        return this != RUC || rucValido(numero);
    }

    private static final int[] PESOS_RUC = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    /** Prefijos de RUC vigentes (10, 15, 16, 17 y 20) y dígito verificador módulo 11. */
    private static boolean rucValido(String ruc) {
        String prefijo = ruc.substring(0, 2);
        if (!prefijo.equals("10") && !prefijo.equals("15") && !prefijo.equals("16")
                && !prefijo.equals("17") && !prefijo.equals("20")) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < PESOS_RUC.length; i++) {
            suma += (ruc.charAt(i) - '0') * PESOS_RUC[i];
        }
        int verificador = (11 - suma % 11) % 10;
        return verificador == ruc.charAt(10) - '0';
    }
}
