package com.ejercicio2.energysaver.ui.Clase;

import java.io.Serializable;

public class AhorroHogar implements Serializable {
    private String titulo;
    private String descripcion;
    private String imagenUrl;
    private String fecha;

    // Constructor vacío requerido para Firebase
    public AhorroHogar() {
        // Firebase requires an empty constructor
    }

    // Constructor con parámetros
    public AhorroHogar(String titulo, String descripcion, String imagenUrl, String fecha) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.fecha = fecha;
    }

    // Getters y Setters
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
