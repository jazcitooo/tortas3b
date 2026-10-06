package py.tortas3b.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import py.tortas3b.service.DashboardService;

/**
 * Endpoint REST para refresco automático asíncrono del dashboard cada 60 segundos.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerMetricas() {
        return ResponseEntity.ok(dashboardService.obtenerMetricas());
    }
}
