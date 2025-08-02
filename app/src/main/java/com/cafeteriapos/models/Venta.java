package com.cafeteriapos.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    private String id;
    private LocalDateTime fechaHora;
    private List<Producto> items;
    private double total;

    // Constructor completo
    public Venta(String id, LocalDateTime fechaHora, List<Producto> items, double total) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.items = items != null ? items : new ArrayList<>();
        this.total = total;
    }

    // Constructor vacío para simulaciones/temporal
    public Venta() {
        this.id = "";
        this.fechaHora = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.total = 0.0;
    }

    // Getters
    public String getId() { return id; }
    public double getTotal() { return total; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public List<Producto> getItems() { return items; }
    
    // Método conveniente para obtener solo la fecha
    public LocalDate getFecha() { 
        return fechaHora.toLocalDate(); 
    }

    // Setters para simulaciones
    public void setId(String id) { this.id = id; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public void setFecha(LocalDate fecha) { 
        this.fechaHora = fecha.atStartOfDay(); 
    }
    public void setItems(List<Producto> items) { this.items = items; }
    public void setTotal(double total) { this.total = total; }
}