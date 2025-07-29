package com.cafeteriapos.models;

import javafx.beans.property.*;

public class Producto {
    private final StringProperty nombre;
    private final DoubleProperty precio;

    public Producto(String nombre, double precio) {
        this.nombre = new SimpleStringProperty(nombre);
        this.precio = new SimpleDoubleProperty(precio);
    }

    // Getters para Property (necesarios para TableView)
    public StringProperty nombreProperty() { return nombre; }
    public DoubleProperty precioProperty() { return precio; }

    // Getters estándar
    public String getNombre() { return nombre.get(); }
    public double getPrecio() { return precio.get(); }
}