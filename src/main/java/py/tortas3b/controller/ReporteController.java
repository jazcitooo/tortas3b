package py.tortas3b.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import py.tortas3b.service.ReporteService;

/**
 * Generación de reportes analíticos con filtros e impresión amigable (CSS print).
 */
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/entregas")
    public String reporteEntregas(@RequestParam(value = "fechaDesde", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
                                  @RequestParam(value = "fechaHasta", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
                                  @RequestParam(value = "estado", required = false) String estado,
                                  Model model) {
        if (fechaDesde == null && fechaHasta == null) {
            fechaDesde = LocalDate.now();
            fechaHasta = LocalDate.now();
        }
        model.addAttribute("entregas", reporteService.obtenerEntregas(fechaDesde, fechaHasta, estado));
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("estadoFiltro", estado);
        return "reportes/entregas";
    }

    @GetMapping("/ganancias")
    public String reporteGanancias(@RequestParam(value = "fechaDesde", required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
                                   @RequestParam(value = "fechaHasta", required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
                                   Model model) {
        if (fechaDesde == null && fechaHasta == null) {
            fechaDesde = LocalDate.now().minusDays(30);
            fechaHasta = LocalDate.now();
        }
        model.addAttribute("reporte", reporteService.obtenerReporteGanancias(fechaDesde, fechaHasta));
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        return "reportes/ganancias";
    }

    @GetMapping("/productos-mas-vendidos")
    public String reporteProductosMasVendidos(@RequestParam(value = "fechaDesde", required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
                                              @RequestParam(value = "fechaHasta", required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
                                              @RequestParam(value = "limite", defaultValue = "10") int limite,
                                              Model model) {
        if (fechaDesde == null && fechaHasta == null) {
            fechaDesde = LocalDate.now().minusDays(30);
            fechaHasta = LocalDate.now();
        }
        model.addAttribute("ranking", reporteService.obtenerProductosMasVendidos(fechaDesde, fechaHasta, limite));
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("limite", limite);
        return "reportes/productos-mas-vendidos";
    }
}
