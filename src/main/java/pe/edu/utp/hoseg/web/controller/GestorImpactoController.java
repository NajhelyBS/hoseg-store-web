package pe.edu.utp.hoseg.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.hoseg.web.config.AdministradorAutenticado;
import pe.edu.utp.hoseg.web.service.GestorImpactoService;
import pe.edu.utp.hoseg.web.service.NegocioException;

import java.util.List;

@Controller
@RequestMapping("/gestion-impacto")
public class GestorImpactoController {

    private final GestorImpactoService gestorImpactoService;

    public GestorImpactoController(GestorImpactoService gestorImpactoService) {
        this.gestorImpactoService = gestorImpactoService;
    }

    @GetMapping("/lotes")
    public String lotes(Model model) {
        model.addAttribute("lotes", gestorImpactoService.lotes());
        model.addAttribute("pendientes", gestorImpactoService.donacionesPendientes());
        model.addAttribute("comunidades", gestorImpactoService.comunidades());
        model.addAttribute("ongs", gestorImpactoService.ongs());
        return "impacto/lotes";
    }

    @PostMapping("/lotes")
    public String crearLote(@AuthenticationPrincipal AdministradorAutenticado usuario,
                            @RequestParam Integer idComunidad,
                            @RequestParam Integer idOng,
                            @RequestParam(required = false) String observaciones,
                            RedirectAttributes flash) {
        try {
            var lote = gestorImpactoService.crearLote(idComunidad, idOng, usuario.id(), observaciones);
            flash.addFlashAttribute("exito", "Lote " + lote.getCodigoLote() + " creado. Ahora asígnale donaciones pendientes.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion-impacto/lotes";
    }

    @PostMapping("/lotes/{id}/asignar")
    public String asignar(@PathVariable Integer id,
                          @RequestParam(name = "idsDonacion", required = false) List<Integer> idsDonacion,
                          RedirectAttributes flash) {
        try {
            int n = gestorImpactoService.asignarDonaciones(id, idsDonacion);
            flash.addFlashAttribute("exito", "Se asignaron " + n + " donación(es) al lote.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion-impacto/lotes";
    }

    @PostMapping("/lotes/{id}/despachar")
    public String despachar(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            gestorImpactoService.despacharLote(id);
            flash.addFlashAttribute("exito", "Lote marcado como En Ruta hacia la comunidad.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion-impacto/lotes";
    }

    @PostMapping("/lotes/{id}/entregar")
    public String entregar(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            gestorImpactoService.entregarLote(id);
            flash.addFlashAttribute("exito", "Lote entregado. Las donaciones y el stock comprometido se actualizaron.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion-impacto/lotes";
    }

        @GetMapping(value = "/lotes.csv", produces = "text/csv;charset=UTF-8")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<String> exportarCsv() {
        StringBuilder sb = new StringBuilder("\uFEFFCodigo Lote;Comunidad;ONG Aliada;Estado;Fecha Creacion;Observaciones\n");
        for (var l : gestorImpactoService.lotes()) {
            sb.append(l.getCodigoLote()).append(';')
              .append(l.getComunidad().getNombre()).append(';')
              .append(l.getOng().getNombre()).append(';')
              .append(l.getEstado().name()).append(';')
              .append(l.getFechaCreacion().toLocalDate()).append(';')
              .append(l.getObservaciones() != null ? l.getObservaciones().replace(';', ',') : "")
              .append('\n');
        }
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte-Impacto-Hoseg.csv\"")
                .body(sb.toString());
    }

}
