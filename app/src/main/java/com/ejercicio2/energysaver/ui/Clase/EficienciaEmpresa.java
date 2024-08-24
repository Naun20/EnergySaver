package com.ejercicio2.energysaver.ui.Clase;

import java.io.Serializable;

public class EficienciaEmpresa implements Serializable {

    private String titulo;
    private String descripcion;
    private String fecha;
    private String imagenUrl;

    // Constructor vacío requerido para Firebase
    public EficienciaEmpresa() {
    }

    // Constructor con todos los campos
    public EficienciaEmpresa(String titulo, String descripcion, String fecha, String imagenUrl) {
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
}
