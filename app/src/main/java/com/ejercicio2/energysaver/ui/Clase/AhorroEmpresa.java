package com.ejercicio2.energysaver.ui.Clase;

import java.io.Serializable;

public class AhorroEmpresa implements Serializable {

    private String titulo;
    private String descripcion;
    private String fecha;
    private String imagenUrl;

    public AhorroEmpresa() {
        // Constructor vacío requerido por Firestore
    }

    public AhorroEmpresa(String titulo, String descripcion, String fecha, String imagenUrl) {
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
        return "AhorroEmpresa{" +
                "titulo='" + titulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fecha='" + fecha + '\'' +
                ", imagenUrl='" + imagenUrl + '\'' +
                '}';
    }
}
