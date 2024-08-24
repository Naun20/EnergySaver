package com.ejercicio2.energysaver.ui.Clase;

import java.io.Serializable;

public class IndustriaEficiencia implements Serializable {
    private String titulo;
    private String descripcion;
    private String imagenUrl;
    private String fecha;

    // Constructor vacío necesario para la deserialización
    public IndustriaEficiencia() {}

    public IndustriaEficiencia(String titulo, String descripcion, String imagenUrl, String fecha) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.fecha = fecha;
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
