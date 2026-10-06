package py.tortas3b.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import py.tortas3b.service.DashboardService;
import py.tortas3b.util.SecurityUtil;

/**
 * Pantalla principal del sistema con métricas operativas y entregas del día.
 */
@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Map<String, Object> metricas = dashboardService.obtenerMetricas();
        model.addAllAttributes(metricas);
        model.addAttribute("usuarioNombre", SecurityUtil.getNombreActual());
        model.addAttribute("usuarioRol", SecurityUtil.getRolActual());
        model.addAttribute("esAdmin", SecurityUtil.esAdmin());
        return "dashboard";
    }
}
