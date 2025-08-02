package com.cafeteriapos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas simplificada para el Sistema POS
 * Punto de entrada para validar que todos los componentes funcionan
 */
@DisplayName("Sistema POS - Validación General")
class AllTestsSuite {
    
    @Test
    @DisplayName("Validación de que todas las clases principales se pueden instanciar")
    void testClasesPrincipalesInstanciables() {
        // When & Then - Las clases principales deben poder instanciarse
        assertDoesNotThrow(() -> {
            // Test modelos
            new com.cafeteriapos.models.Producto("Test", 1.0, 1);
            new com.cafeteriapos.models.Venta();
            
            // Test que las clases Utils tienen métodos estáticos accesibles
            com.cafeteriapos.utils.ExcelManager.leerProductos();
            
        }, "Las clases principales deben ser instanciables");
    }
    
    @Test
    @DisplayName("Validación de que el sistema maneja errores graciosamente")
    void testManejoErroresGeneral() {
        // When & Then - El sistema no debe fallar con operaciones básicas
        assertDoesNotThrow(() -> {
            // Operaciones que no deben fallar nunca
            com.cafeteriapos.utils.ExcelManager.leerProductos();
            com.cafeteriapos.utils.ExcelManager.leerVentas();
            com.cafeteriapos.utils.CajaManager.registrarError("Test", "Test Error");
            
        }, "El sistema debe manejar operaciones básicas sin fallar");
    }
}
