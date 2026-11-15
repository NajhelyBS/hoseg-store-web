package pe.edu.utp.hoseg.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pe.edu.utp.hoseg.web.dto.DocumentoConsultado;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
@Primary
public class ConsultaDocumentoHttpService implements ConsultaDocumentoService {

    private final RestClient restClient;
    private final String token;
    private final ConsultaDocumentoSimulador simulador;

    public ConsultaDocumentoHttpService(
            @Value("${hoseg.documento-api.url:https://decolecta.com}") String baseUrl,
            @Value("${hoseg.documento-api.token:${DOCUMENTO_API_TOKEN:}}") String token,
            ConsultaDocumentoSimulador simulador) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
        this.token = token == null ? "" : token.trim();
        this.simulador = simulador;
    }

    @Override
    public Optional<DocumentoConsultado> consultar(String tipo, String numero) {
        if (tipo == null || numero == null) {
            return Optional.empty();
        }
        String tipoNorm = tipo.trim().toUpperCase();
        String numNorm = numero.trim();

        if (!("DNI".equals(tipoNorm) && numNorm.matches("^[0-9]{8}$"))
                && !("RUC".equals(tipoNorm) && numNorm.matches("^[0-9]{11}$"))) {
            return Optional.empty();
        }

        if (token.isBlank()) {
            return simulador.consultar(tipoNorm, numNorm);
        }

        try {
            String path = "DNI".equals(tipoNorm)
                    ? "/reniec/dni?numero=" + numNorm
                    : "/sunat/ruc?numero=" + numNorm;

            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri(path)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(Map.class);

            if (body == null || body.isEmpty()) {
                return simulador.consultar(tipoNorm, numNorm);
            }

            if ("DNI".equals(tipoNorm)) {
                String nombres = str(body, "first_name", "nombres");
                String apPat = str(body, "first_last_name", "apellidoPaterno");
                String apMat = str(body, "second_last_name", "apellidoMaterno");
                String apellidos = (apPat + " " + apMat).trim();
                String completo = str(body, "full_name", "nombreCompleto");
                if (completo.isBlank()) completo = (nombres + " " + apellidos).trim();
                if (!completo.isBlank()) {
                    return Optional.of(new DocumentoConsultado("DNI", numNorm, nombres, apellidos, completo, "", "RENIEC"));
                }
            } else {
                String razon = str(body, "razon_social", "razonSocial", "nombre");
                String dir = str(body, "direccion");
                if (!razon.isBlank()) {
                    return Optional.of(new DocumentoConsultado("RUC", numNorm, razon, "", razon, dir, "SUNAT"));
                }
            }
        } catch (Exception ignored) {
            // Si falla la red o el proveedor externo, responde el simulador sin interrumpir al usuario.
        }
        return simulador.consultar(tipoNorm, numNorm);
    }

    private static String str(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null && !v.toString().isBlank()) {
                return v.toString().trim();
            }
        }
        return "";
    }
}
