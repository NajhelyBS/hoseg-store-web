package pe.edu.utp.hoseg.web.config;

import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.OffsetDateTime;

/**
 * Formatea fechas para las plantillas en hora de Lima. Postgres entrega los
 * TIMESTAMPTZ como instantes (en UTC), y imprimirlos tal cual muestra la hora
 * de Greenwich: 8 p. m. en Lima saldría como 1 a. m. del día siguiente.
 * Uso en plantillas: <code>${@fechas.formato(p.fecha, 'dd/MM/yyyy HH:mm')}</code>.
 */
@Component("fechas")
public class FormatoFechas {

    public static final ZoneId LIMA = ZoneId.of("America/Lima");

    public String formato(TemporalAccessor fecha, String patron) {
        if (fecha == null) {
            return null;
        }
        TemporalAccessor enLima = fecha instanceof OffsetDateTime o
                ? o.atZoneSameInstant(LIMA)
                : fecha;
        return DateTimeFormatter.ofPattern(patron).format(enLima);
    }
}
