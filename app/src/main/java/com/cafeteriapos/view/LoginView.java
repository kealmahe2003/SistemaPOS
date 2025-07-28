package com.cafeteriapos.view;

import com.cafeteriapos.controller.LoginController;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class LoginView {
    private VBox view;
    private TextField usuarioField;
    private PasswordField passwordField;

    public LoginView() {
        view = new VBox(10);
        view.setPadding(new Insets(20));

        Label titulo = new Label("Inicio de Sesión");
        titulo.setFont(new Font(18));

        usuarioField = new TextField();
        usuarioField.setPromptText("Usuario");

        passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");

        Button loginBtn = new Button("Ingresar");
        loginBtn.setOnAction(e -> {
            String usuario = usuarioField.getText();
            String clave = passwordField.getText();
            new LoginController().verificarCredenciales(usuario, clave);
        });

        view.getChildren().addAll(titulo, usuarioField, passwordField, loginBtn);
    }

    public VBox getView() {
        return view;
    }
}