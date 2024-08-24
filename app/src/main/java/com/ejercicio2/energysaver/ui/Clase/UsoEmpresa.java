package com.ejercicio2.energysaver.ui.Clase;

import java.io.Serializable;

public class UsoEmpresa implements Serializable {

    private String titulo;
    private String descripcion;
    private String fecha;
    private String imagenUrl;

    // Constructor vacío requerido para la deserialización
    public UsoEmpresa() {
    }

    // Constructor con parámetros
    public UsoEmpresa(String titulo, String descripcion, String fecha, String imagenUrl) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.imagenUrl = imagenUrl;
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
        return "UsoEmpresa{" +
                "titulo='" + titulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fecha='" + fecha + '\'' +
                ", imagenUrl='" + imagenUrl + '\'' +
                '}';
    }
}
