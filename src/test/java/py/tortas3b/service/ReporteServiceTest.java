package py.tortas3b.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import py.tortas3b.repository.ReporteRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @InjectMocks
    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reporteService, "porcentajeGanancia", 0.38);
    }

    @Test
    @DisplayName("Regla de ganancia: calcula exactamente el 38% sobre los ingresos del día")
    void debeCalcularGananciaTreintaYOchoPorCiento() {
        LocalDate desde = LocalDate.now().minusDays(1);
        LocalDate hasta = LocalDate.now();

        Map<String, Object> fila1 = new HashMap<>();
        fila1.put("fecha", java.sql.Date.valueOf(desde));
        fila1.put("cantidad_ventas", 2L);
        fila1.put("total_ingresos", new BigDecimal("1000000")); // 1.000.000 Gs.
        fila1.put("total_kilos", new BigDecimal("5.500"));

        when(reporteRepository.obtenerGananciasDiarias(desde, hasta))
                .thenReturn(List.of(fila1));

        Map<String, Object> reporte = reporteService.obtenerReporteGanancias(desde, hasta);

        assertNotNull(reporte);
        assertEquals(38, reporte.get("porcentaje"));
        assertEquals(2L, reporte.get("totalVentas"));
        assertEquals(new BigDecimal("1000000"), reporte.get("totalIngresos"));
        assertEquals(new BigDecimal("380000"), reporte.get("totalGanancias")); // 1.000.000 x 0.38 = 380.000

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> filas = (List<Map<String, Object>>) reporte.get("filas");
        assertEquals(1, filas.size());
        assertEquals(new BigDecimal("380000"), filas.get(0).get("ganancia"));
    }
}
