package pe.edu.utp.hoseg.web.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoDocumentoTest {

    @Test
    void rucConDigitoVerificadorCorrectoSeAcepta() {
        assertTrue(TipoDocumento.RUC.admite("20100070970"));
    }

    @Test
    void rucConDigitoVerificadorIncorrectoSeRechaza() {
        assertFalse(TipoDocumento.RUC.admite("20100070971"));
    }

    @Test
    void rucConPrefijoInexistenteSeRechaza() {
        assertFalse(TipoDocumento.RUC.admite("99999999999"));
    }

    @Test
    void rucConLongitudIncorrectaSeRechaza() {
        assertFalse(TipoDocumento.RUC.admite("2010007097"));
    }

    @Test
    void dniSoloExigeOchoDigitos() {
        assertTrue(TipoDocumento.DNI.admite("12345678"));
        assertFalse(TipoDocumento.DNI.admite("1234567"));
        assertFalse(TipoDocumento.DNI.admite("1234567A"));
    }
}
