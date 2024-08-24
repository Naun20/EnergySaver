package com.ejercicio2.energysaver.ui.Clase;

import java.io.Serializable;

public class Calculo implements Serializable {
    private String titulo;
    private String descripcion;
    private String fecha;
    private String imagenUrl;

    public Calculo() {
        // Constructor vacío necesario para deserialización
    }

    public Calculo(String titulo, String descripcion, String fecha, String imagenUrl) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
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

    @Override
    public String toString() {
        return "Calculo{" +
                "titulo='" + titulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fecha='" + fecha + '\'' +
                ", imagenUrl='" + imagenUrl + '\'' +
                '}';
    }
}

