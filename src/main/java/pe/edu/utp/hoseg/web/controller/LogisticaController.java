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
import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.service.LogisticaService;
import pe.edu.utp.hoseg.web.service.NegocioException;

import java.util.List;

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

    @GetMapping("/inventario")
    public String inventario(Model model) {
        List<Producto> productos = logisticaService.inventarioActivo();
        long criticos = productos.stream().filter(p -> p.getStockComercial() <= p.getStockMinimo()).count();
        model.addAttribute("productos", productos);
        model.addAttribute("totalCriticos", criticos);
        return "logistica/inventario";
    }

    @PostMapping("/inventario/{codigo}/reponer")
    public String reponerStock(@PathVariable String codigo,
                               @RequestParam int cantidad,
                               RedirectAttributes flash) {
        try {
            logisticaService.reponerStock(codigo, cantidad);
            flash.addFlashAttribute("exito", "Stock actualizado (+ " + cantidad + " unidades) para " + codigo + ".");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/inventario";
    }

    @GetMapping("/proveedores")
    public String proveedores(Model model) {
        model.addAttribute("proveedores", logisticaService.proveedoresActivos());
        model.addAttribute("ordenes", logisticaService.pedidosProveedores());
        model.addAttribute("productos", logisticaService.inventarioActivo());
        return "logistica/proveedores";
    }

    @PostMapping("/proveedores")
    public String crearProveedor(@RequestParam String nombreTaller,
                                 @RequestParam(required = false) String ruc,
                                 @RequestParam(required = false) String contacto,
                                 @RequestParam(required = false) String telefono,
                                 RedirectAttributes flash) {
        try {
            logisticaService.registrarProveedor(nombreTaller, ruc, contacto, telefono);
            flash.addFlashAttribute("exito", "Proveedor registrado correctamente.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/proveedores";
    }

    @PostMapping("/proveedores/ordenes")
    public String crearOrden(@AuthenticationPrincipal AdministradorAutenticado usuario,
                             @RequestParam Integer idProveedor,
                             @RequestParam(required = false) String codigoProducto,
                             @RequestParam(required = false) String descripcion,
                             @RequestParam int cantidad,
                             RedirectAttributes flash) {
        try {
            logisticaService.crearOrdenProveedor(idProveedor, codigoProducto, descripcion, cantidad, usuario.id());
            flash.addFlashAttribute("exito", "Orden de producción enviada al taller.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/proveedores";
    }

    @PostMapping("/proveedores/ordenes/{id}/recibir")
    public String recibirOrden(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            logisticaService.recibirOrdenProveedor(id);
            flash.addFlashAttribute("exito", "Orden recibida e ingresada automáticamente al inventario.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/proveedores";
    }
}
