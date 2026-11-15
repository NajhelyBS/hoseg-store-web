package pe.edu.utp.hoseg.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.service.LogisticaService;
import pe.edu.utp.hoseg.web.service.NegocioException;

@Controller
@RequestMapping("/logistica")
public class LogisticaController {

    private final LogisticaService logisticaService;

    public LogisticaController(LogisticaService logisticaService) {
        this.logisticaService = logisticaService;
    }

    @GetMapping("/pedidos")
    public String pedidos(@RequestParam(defaultValue = "DELIVERY") String tab, Model model) {
        TipoEnvio tipoActivo = "RECOJO".equalsIgnoreCase(tab) ? TipoEnvio.RECOJO_TIENDA : TipoEnvio.DELIVERY;
        model.addAttribute("tab", tipoActivo == TipoEnvio.RECOJO_TIENDA ? "RECOJO" : "DELIVERY");
        model.addAttribute("pedidos", logisticaService.pedidosPagadosPorTipo(tipoActivo));
        return "logistica/pedidos";
    }

    @PostMapping("/pedidos/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id,
                                @RequestParam EstadoEnvio estado,
                                @RequestParam(required = false) String courier,
                                @RequestParam(required = false) String codigoSeguimiento,
                                @RequestParam(defaultValue = "DELIVERY") String tab,
                                RedirectAttributes flash) {
        try {
            logisticaService.avanzarEstado(id, estado, courier, codigoSeguimiento);
            flash.addFlashAttribute("exito", "Estado del pedido actualizado a " + estado.getEtiqueta() + ".");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/pedidos?tab=" + tab;
    }
}
