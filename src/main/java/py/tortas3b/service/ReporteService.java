package py.tortas3b.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import py.tortas3b.repository.ReporteRepository;

/**
 * Servicio de reportes que procesa entregas, cálculo de ganancia (38%)
 * y estadísticas de ventas.
 */
@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;

    @Value("${negocio.porcentaje-ganancia:0.38}")
    private double porcentajeGanancia;

    public ReporteService(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    public double getPorcentajeGanancia() {
        return porcentajeGanancia;
    }

    public List<Map<String, Object>> obtenerEntregas(LocalDate fechaDesde, LocalDate fechaHasta, String estado) {
        return reporteRepository.obtenerEntregas(fechaDesde, fechaHasta, estado);
    }

    public Map<String, Object> obtenerReporteGanancias(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Map<String, Object>> filasDb = reporteRepository.obtenerGananciasDiarias(fechaDesde, fechaHasta);
        List<Map<String, Object>> filasProcesadas = new ArrayList<>();

        long totalVentasGlobal = 0;
        BigDecimal totalIngresosGlobal = BigDecimal.ZERO;
        BigDecimal totalKilosGlobal = BigDecimal.ZERO;
        BigDecimal totalGananciasGlobal = BigDecimal.ZERO;

        BigDecimal factorGanancia = BigDecimal.valueOf(porcentajeGanancia);

        for (Map<String, Object> fila : filasDb) {
            Map<String, Object> filaNueva = new HashMap<>(fila);

            Number cantVentas = (Number) fila.get("cantidad_ventas");
            Number ingresos = (Number) fila.get("total_ingresos");
            Number kilos = (Number) fila.get("total_kilos");

            long vCount = cantVentas != null ? cantVentas.longValue() : 0;
            BigDecimal ing = ingresos != null ? new BigDecimal(ingresos.toString()) : BigDecimal.ZERO;
            BigDecimal kg = kilos != null ? new BigDecimal(kilos.toString()) : BigDecimal.ZERO;
            BigDecimal ganancia = ing.multiply(factorGanancia).setScale(0, RoundingMode.HALF_UP);

            filaNueva.put("ganancia", ganancia);

            totalVentasGlobal += vCount;
            totalIngresosGlobal = totalIngresosGlobal.add(ing);
            totalKilosGlobal = totalKilosGlobal.add(kg);
            totalGananciasGlobal = totalGananciasGlobal.add(ganancia);

            filasProcesadas.add(filaNueva);
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("filas", filasProcesadas);
        resultado.put("totalVentas", totalVentasGlobal);
        resultado.put("totalIngresos", totalIngresosGlobal);
        resultado.put("totalKilos", totalKilosGlobal);
        resultado.put("totalGanancias", totalGananciasGlobal);
        resultado.put("porcentaje", (int) Math.round(porcentajeGanancia * 100));

        return resultado;
    }

    public List<Map<String, Object>> obtenerProductosMasVendidos(LocalDate fechaDesde, LocalDate fechaHasta, int limite) {
        return reporteRepository.obtenerProductosMasVendidos(fechaDesde, fechaHasta, limite > 0 ? limite : 10);
    }
}
