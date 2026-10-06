package py.tortas3b.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import py.tortas3b.service.DeliveryService;

/**
 * Gestión del módulo de delivery y rutas de distribución.
 */
@Controller
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "fecha", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                         @RequestParam(value = "estado", required = false) String estado,
                         Model model) {
        model.addAttribute("deliveries", deliveryService.listar(fecha, estado));
        model.addAttribute("fechaFiltro", fecha);
        model.addAttribute("estadoFiltro", estado);
        return "delivery/lista";
    }

    @PostMapping("/estado/{pedidoId}")
    public String cambiarEstado(@PathVariable("pedidoId") Integer pedidoId,
                                @RequestParam("estado") String estado,
                                RedirectAttributes flash) {
        try {
            deliveryService.cambiarEstado(pedidoId, estado);
            flash.addFlashAttribute("exito", "Estado de entrega a domicilio actualizado a " + estado + ".");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/delivery";
    }
}
