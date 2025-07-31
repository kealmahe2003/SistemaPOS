package com.cafeteriapos.models;

import javafx.beans.property.*;

public class Producto {
    private final StringProperty nombre;
    private final DoubleProperty precio;
    private final IntegerProperty stock;

    public Producto(String nombre, double precio, int stock) {
        this.nombre = new SimpleStringProperty(nombre);
        this.precio = new SimpleDoubleProperty(precio);
        this.stock = new SimpleIntegerProperty(stock);
    }

    // Getters para Property (necesarios para TableView)
    public StringProperty nombreProperty() { return nombre; }
    public DoubleProperty precioProperty() { return precio; }
    public IntegerProperty stockProperty() { return stock; }

    // Getters estándar
    public String getNombre() { return nombre.get(); }
    public double getPrecio() { return precio.get(); }
    public int getStock() { return stock.get(); }

    // Setters
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public void setPrecio(double precio) { this.precio.set(precio); }
    public void setStock(int stock) { this.stock.set(stock); }
}