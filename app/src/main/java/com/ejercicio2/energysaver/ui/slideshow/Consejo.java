package com.ejercicio2.energysaver.ui.slideshow;

public class Consejo {
    private String id; // ID del documento
    private String titulo;
    private String fecha;
    private String descripcion;
    private String imagenUrl;

    // Constructor vacío requerido para Firebase
    public Consejo() {
    }

    // Constructor con parámetros
    public Consejo(String id, String titulo, String fecha, String descripcion, String imagenUrl) {
        this.id = id;
        this.titulo = titulo;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
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
}

