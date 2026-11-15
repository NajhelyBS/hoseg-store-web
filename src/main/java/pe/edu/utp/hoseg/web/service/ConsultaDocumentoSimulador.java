package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Component;
import pe.edu.utp.hoseg.web.dto.DocumentoConsultado;

import java.util.Map;
import java.util.Optional;

/** Respaldo local para que la demo nunca dependa de internet o cuotas de API externa. */
@Component
public class ConsultaDocumentoSimulador {

    private static final Map<String, DocumentoConsultado> PADRON_DEMO = Map.of(
            "45678912", new DocumentoConsultado("DNI", "45678912", "ANA LUCÍA", "TORRES MENDOZA",
                    "ANA LUCÍA TORRES MENDOZA", "AV. EL SOL 450, CUSCO", "SIMULADOR"),
            "71234568", new DocumentoConsultado("DNI", "71234568", "CARLOS ALBERTO", "QUISPE HUAMÁN",
                    "CARLOS ALBERTO QUISPE HUAMÁN", "JR. UNION 210, CUSCO", "SIMULADOR"),
            "20601234567", new DocumentoConsultado("RUC", "20601234567", "HÖSÉG STORE PERÚ S.A.C.", "",
                    "HÖSÉG STORE PERÚ S.A.C.", "CALLE PALACIO 110, CUSCO", "SIMULADOR"),
            "20100047218", new DocumentoConsultado("RUC", "20100047218", "BANCO DE CRÉDITO DEL PERÚ", "",
                    "BANCO DE CRÉDITO DEL PERÚ", "CALLE CENTENARIO 156, LA MOLINA", "SIMULADOR")
    );

    public Optional<DocumentoConsultado> consultar(String tipo, String numero) {
        if (PADRON_DEMO.containsKey(numero)) {
            return Optional.of(PADRON_DEMO.get(numero));
        }
        if ("DNI".equals(tipo) && numero.matches("^[0-9]{8}$")) {
            return Optional.of(new DocumentoConsultado("DNI", numero, "VALERIA SOFÍA", "CCAHUANA VARGAS",
                    "VALERIA SOFÍA CCAHUANA VARGAS", "", "SIMULADOR"));
        }
        if ("RUC".equals(tipo) && numero.matches("^(10|20)[0-9]{9}$")) {
            return Optional.of(new DocumentoConsultado("RUC", numero, "INVERSIONES ANDINAS S.A.C.", "",
                    "INVERSIONES ANDINAS S.A.C.", "AV. LA CULTURA 1200, CUSCO", "SIMULADOR"));
        }
        return Optional.empty();
    }
}
