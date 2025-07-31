package com.cafeteriapos.models;

import java.time.LocalDateTime;
import java.util.List;

public class Venta {
    private final String id;
    private final LocalDateTime fecha;
    private final List<Producto> items;
    private final double total;

    public Venta(String id, LocalDateTime fecha, List<Producto> items, double total) {
        this.id = id;
        this.fecha = fecha;
        this.items = items;
        this.total = total;
    }

    // Getters
    public String getId() { return id; }
    public double getTotal() { return total; }
    public LocalDateTime getFecha() { return fecha; }
    public List<Producto> getItems() { return items; }
}