package com.cafeteriapos.controller;


import com.cafeteriapos.utils.CajaManager;

import javafx.scene.control.Alert;

public class LoginController {

    public void verificarCredenciales(String usuario, String clave) {
        if (usuario.equals("admin") && clave.equals("1234")) {
            CajaManager.abrirCaja();
            mostrarAlerta("Login exitoso", "Bienvenido admin. Caja abierta.");
            // Aquí puedes redirigir a otra ventana si quieres
        } else {
            mostrarAlerta("Error", "Usuario o contraseña incorrecta");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}