package com.ejercicio2.energysaver.ui.home;

public class Novedad {
    private String titulo;
    private String descripcion;
    private String enlace;
    private String imageUrl;

    public Novedad() {
        // Constructor vacío requerido para Firestore
    }

    public Novedad(String titulo, String descripcion, String enlace, String imageUrl) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.enlace = enlace;
        this.imageUrl = imageUrl;
    }

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

    public String getEnlace() {
        return enlace;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
