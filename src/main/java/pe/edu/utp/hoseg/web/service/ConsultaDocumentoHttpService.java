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

    public ConsultaDocumentoHttpService(
            @Value("${hoseg.documento-api.url:https://api.decolecta.com/v1}") String baseUrl,
            @Value("${hoseg.documento-api.token:${DOCUMENTO_API_TOKEN:}}") String token) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();

        this.token = token == null ? "" : token.trim();
    }

    @Override
    public Optional<DocumentoConsultado> consultar(String tipo, String numero) {

        if (tipo == null || numero == null || token.isBlank()) {
            return Optional.empty();
        }

        String tipoNorm = tipo.trim().toUpperCase();
        String numNorm = numero.trim();

        if (!("DNI".equals(tipoNorm) && numNorm.matches("^[0-9]{8}$"))
                && !("RUC".equals(tipoNorm) && numNorm.matches("^[0-9]{11}$"))) {
            return Optional.empty();
        }

        try {

            String endpoint = "DNI".equals(tipoNorm)
                    ? "/reniec/dni?numero={numero}"
                    : "/sunat/ruc?numero={numero}";

            Map<?, ?> body = restClient.get()
                    .uri(endpoint, numNorm)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(Map.class);

            if (body == null || body.isEmpty()) {
                return Optional.empty();
            }

            if ("DNI".equals(tipoNorm)) {

                String nombres = str(body, "first_name");
                String apellidoPaterno = str(body, "first_last_name");
                String apellidoMaterno = str(body, "second_last_name");

                String apellidos = (apellidoPaterno + " " + apellidoMaterno).trim();

                String nombreCompleto = str(body, "full_name");

                if (nombreCompleto.isBlank()) {
                    nombreCompleto = (nombres + " " + apellidos).trim();
                }

                if (nombres.isBlank() || apellidos.isBlank()) {
                    return Optional.empty();
                }

                return Optional.of(
                        new DocumentoConsultado(
                                "DNI",
                                numNorm,
                                nombres,
                                apellidos,
                                nombreCompleto,
                                "",
                                "DECOLECTA - RENIEC"));

            } else {

                String razonSocial = str(body, "razon_social");
                String direccion = str(body, "direccion");

                if (razonSocial.isBlank()) {
                    return Optional.empty();
                }

                return Optional.of(
                        new DocumentoConsultado(
                                "RUC",
                                numNorm,
                                razonSocial,
                                "",
                                razonSocial,
                                direccion,
                                "DECOLECTA - SUNAT"));
            }

        } catch (Exception e) {

            System.err.println(
                    "Error al consultar Decolecta: "
                            + e.getClass().getSimpleName());

            return Optional.empty();
        }
    }

    private static String str(Map<?, ?> map, String key) {

        Object value = map.get(key);
        return value == null ? "" : value.toString().trim();
    }
}
