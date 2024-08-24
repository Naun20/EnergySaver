package com.ejercicio2.energysaver.ui.home;

public class Noticia {
    private String titulo;
    private String fecha;
    private String imagenUrl;
    private String descripcion;
    private String enlace;

    public Noticia() {
        // Constructor vacío requerido para Firebase
    }

    public Noticia(String titulo, String fecha, String imagenUrl, String descripcion, String enlace) {
        this.titulo = titulo;
        this.fecha = fecha;
        this.imagenUrl = imagenUrl;
        this.descripcion = descripcion;
        this.enlace = enlace;
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

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
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
}
