package com.ejercicio2.energysaver.ui.Clase;

import java.io.Serializable;

public class Eficiencia implements Serializable {
    private String titulo;
    private String descripcion;
    private String imagenUrl;

    public Eficiencia() {
        // Constructor vacío requerido para Firebase
    }

    public Eficiencia(String titulo, String descripcion, String imagenUrl) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
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
}
