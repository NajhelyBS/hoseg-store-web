package pe.edu.utp.hoseg.web.config;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FormatoFechasTest {

    private final FormatoFechas fechas = new FormatoFechas();

    @Test
    void conviertaUtcAHoraDeLima() {
        OffsetDateTime utc = OffsetDateTime.of(2026, 10, 30, 1, 5, 0, 0, ZoneOffset.UTC);
        assertEquals("29/10/2026 20:05", fechas.formato(utc, "dd/MM/yyyy HH:mm"));
    }

    @Test
    void laFechaNoSaltaDeDiaPorLaNoche() {
        OffsetDateTime utc = OffsetDateTime.of(2026, 10, 30, 3, 0, 0, 0, ZoneOffset.UTC);
        assertEquals("29/10/2026", fechas.formato(utc, "dd/MM/yyyy"));
    }

    @Test
    void fechaNulaDevuelveNulo() {
        assertNull(fechas.formato(null, "dd/MM/yyyy"));
    }
}
