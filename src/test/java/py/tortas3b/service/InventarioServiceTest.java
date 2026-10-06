package py.tortas3b.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import py.tortas3b.model.Ingrediente;
import py.tortas3b.model.MovimientoStock;
import py.tortas3b.repository.IngredienteRepository;
import py.tortas3b.repository.MovimientoStockRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @InjectMocks
    private InventarioService inventarioService;

    @Test
    @DisplayName("Regla de stock: rechaza ajustes que dejen el inventario en negativo")
    void debeRechazarAjusteQueDejeStockNegativo() {
        Ingrediente ing = new Ingrediente();
        ing.setId(5);
        ing.setNombre("Dulce de Leche");
        ing.setUnidad("KG");
        ing.setStockActual(new BigDecimal("3.000"));

        when(ingredienteRepository.findById(5)).thenReturn(Optional.of(ing));

        // Intento de restar 4 kg cuando solo hay 3 kg
        BigDecimal ajusteNegativo = new BigDecimal("-4.000");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                inventarioService.ajustarStock(5, 1, ajusteNegativo, "Error de tipeo"));

        assertTrue(ex.getMessage().contains("dejaría el stock en negativo"));
        verify(ingredienteRepository, never()).actualizarStock(anyInt(), any(BigDecimal.class));
        verify(movimientoStockRepository, never()).insertar(any(MovimientoStock.class));
    }

    @Test
    @DisplayName("Ajuste válido: actualiza stock e inserta auditoría en movimientos_stock")
    void debePermitirAjusteValidoYRegistrarMovimiento() {
        Ingrediente ing = new Ingrediente();
        ing.setId(5);
        ing.setNombre("Dulce de Leche");
        ing.setUnidad("KG");
        ing.setStockActual(new BigDecimal("3.000"));

        when(ingredienteRepository.findById(5)).thenReturn(Optional.of(ing));

        // Sumar 2.500 kg
        BigDecimal ajuste = new BigDecimal("2.500");

        inventarioService.ajustarStock(5, 1, ajuste, "Ingreso de emergencia");

        verify(ingredienteRepository).actualizarStock(eq(5), eq(new BigDecimal("5.500")));
        verify(movimientoStockRepository).insertar(argThat(mov ->
                mov.getIngredienteId().equals(5) &&
                mov.getCantidad().equals(ajuste) &&
                mov.getStockResultante().equals(new BigDecimal("5.500")) &&
                "AJUSTE".equals(mov.getTipo())
        ));
    }
}
