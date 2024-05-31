package com.app.doggyworld;

public class CartProduct {
    private int id;
    private String nombre;
    private int cantidad;
    private double precio;
    private String imagen;

    public CartProduct(int id, String nombre, int cantidad, double precio, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.imagen = imagen;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
