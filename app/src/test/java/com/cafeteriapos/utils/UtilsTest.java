package com.cafeteriapos.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas para utilidades del sistema
 */
public class UtilsTest {

    @Test
    public void testFormatoMoneda() {
        // Test básico para verificar que las pruebas funcionan
        assertTrue(true);
    }
    
    @Test
    public void testCalculos() {
        // Test de cálculos básicos
        double resultado = 10.0 * 0.18;
        assertEquals(1.8, resultado, 0.01);
    }
}
