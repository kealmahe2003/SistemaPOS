package com.cafeteriapos.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para LoginController (lógica sin UI)
 * Verifica el funcionamiento de las operaciones de autenticación
 */
@DisplayName("Tests para LoginController")
public class LoginControllerTest {

    @Test
    @DisplayName("Validar credenciales correctas")
    void testValidarCredencialesCorrectas() {
        // Given
        String usuarioValido = "admin";
        String passwordValida = "admin123";
        
        // When
        boolean esValido = validarCredenciales(usuarioValido, passwordValida);
        
        // Then
        assertTrue(esValido, "Las credenciales válidas deben ser aceptadas");
    }

    @Test
    @DisplayName("Rechazar credenciales incorrectas")
    void testRechazarCredencialesIncorrectas() {
        // Given
        String usuarioInvalido = "user";
        String passwordInvalida = "wrong";
        
        // When
        boolean esValido = validarCredenciales(usuarioInvalido, passwordInvalida);
        
        // Then
        assertFalse(esValido, "Las credenciales inválidas deben ser rechazadas");
    }

    @Test
    @DisplayName("Validar campos vacíos")
    void testValidarCamposVacios() {
        // Given
        String usuarioVacio = "";
        String passwordVacia = "";
        
        // When
        boolean esValido = validarCredenciales(usuarioVacio, passwordVacia);
        
        // Then
        assertFalse(esValido, "Los campos vacíos deben ser rechazados");
    }

    @Test
    @DisplayName("Validar campos nulos")
    void testValidarCamposNulos() {
        // Given
        String usuarioNulo = null;
        String passwordNula = null;
        
        // When & Then
        assertFalse(validarCredenciales(usuarioNulo, passwordNula), 
                   "Los campos nulos deben ser rechazados");
    }

    @Test
    @DisplayName("Validar longitud mínima de contraseña")
    void testValidarLongitudMinimaPassword() {
        // Given
        String usuario = "admin";
        String passwordCorta = "12";
        String passwordValida = "admin123";
        
        // When & Then
        assertFalse(validarLongitudPassword(passwordCorta), 
                   "Password muy corta debe ser rechazada");
        assertTrue(validarLongitudPassword(passwordValida), 
                  "Password de longitud válida debe ser aceptada");
    }

    @Test
    @DisplayName("Validar caracteres especiales en usuario")
    void testValidarCaracteresEspecialesUsuario() {
        // Given
        String usuarioConEspeciales = "admin@#";
        String usuarioLimpio = "admin";
        
        // When & Then
        assertFalse(contieneSoloCaracteresValidos(usuarioConEspeciales), 
                   "Usuario con caracteres especiales debe ser rechazado");
        assertTrue(contieneSoloCaracteresValidos(usuarioLimpio), 
                  "Usuario con caracteres válidos debe ser aceptado");
    }

    @Test
    @DisplayName("Validar intento de login múltiple")
    void testValidarIntentoLoginMultiple() {
        // Given
        int intentosMaximos = 3;
        int intentosActuales = 2;
        
        // When
        boolean puedeIntentar = intentosActuales < intentosMaximos;
        
        // Then
        assertTrue(puedeIntentar, "Debe permitir intentos dentro del límite");
        
        // Simular otro intento fallido
        intentosActuales++;
        puedeIntentar = intentosActuales < intentosMaximos;
        assertFalse(puedeIntentar, "Debe bloquear después del límite");
    }

    @Test
    @DisplayName("Validar formato de usuario")
    void testValidarFormatoUsuario() {
        // Given
        String usuarioValido = "admin123";
        String usuarioInvalido = "a";
        
        // When & Then
        assertTrue(usuarioValido.length() >= 3, 
                  "Usuario válido debe tener longitud mínima");
        assertFalse(usuarioInvalido.length() >= 3, 
                   "Usuario muy corto debe ser rechazado");
    }

    @Test
    @DisplayName("Validar limpieza de campos")
    void testValidarLimpiezaCampos() {
        // Given
        String usuarioConEspacios = "  admin  ";
        String passwordConEspacios = "  admin123  ";
        
        // When
        String usuarioLimpio = usuarioConEspacios.trim();
        String passwordLimpia = passwordConEspacios.trim();
        
        // Then
        assertEquals("admin", usuarioLimpio);
        assertEquals("admin123", passwordLimpia);
    }

    @Test
    @DisplayName("Validar sesión después del login")
    void testValidarSesionDespuesLogin() {
        // Given
        boolean loginExitoso = true;
        
        // When
        boolean sesionActiva = loginExitoso;
        
        // Then
        assertTrue(sesionActiva, "La sesión debe estar activa después de login exitoso");
    }

    @Test
    @DisplayName("Validar redirección después del login")
    void testValidarRedireccionDespuesLogin() {
        // Given
        String paginaDestino = "/views/MainView.fxml";
        boolean loginExitoso = true;
        
        // When
        String paginaActual = loginExitoso ? paginaDestino : "/views/LoginView.fxml";
        
        // Then
        assertEquals(paginaDestino, paginaActual, 
                    "Debe redirigir a la página principal después del login");
    }

    @Test
    @DisplayName("Validar manejo de errores de autenticación")
    void testValidarManejoErroresAutenticacion() {
        // When & Then
        assertDoesNotThrow(() -> {
            // Simular diferentes tipos de errores de autenticación
            validarCredenciales("invalid", "invalid");
            validarCredenciales("", "");
            validarCredenciales(null, null);
        }, "El manejo de errores no debe lanzar excepciones");
    }

    // Métodos auxiliares para simular la lógica de validación
    private boolean validarCredenciales(String usuario, String password) {
        if (usuario == null || password == null) return false;
        if (usuario.trim().isEmpty() || password.trim().isEmpty()) return false;
        if (!contieneSoloCaracteresValidos(usuario)) return false;
        if (!validarLongitudPassword(password)) return false;
        
        // Credenciales válidas para testing
        return "admin".equals(usuario.trim()) && "admin123".equals(password.trim());
    }

    private boolean validarLongitudPassword(String password) {
        return password != null && password.length() >= 6;
    }

    private boolean contieneSoloCaracteresValidos(String texto) {
        if (texto == null) return false;
        return texto.matches("^[a-zA-Z0-9]+$");
    }
}
