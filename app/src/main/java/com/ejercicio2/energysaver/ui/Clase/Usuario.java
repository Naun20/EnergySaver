package com.ejercicio2.energysaver.ui.Clase;

import java.io.Serializable;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombreCompleto;
    private String nombreUsuario;
    private String email;
    private String dni;
    private String telefono;
    private String tipoUsuario;
    private String imageUrl;
    private String contrasena;

    public Usuario() {
        // Constructor vacío necesario para Firebase
    }

    public Usuario(String nombreCompleto, String nombreUsuario, String email, String dni, String telefono, String tipoUsuario, String imageUrl, String contrasena) {
        this.nombreCompleto = nombreCompleto;
        this.nombreUsuario = nombreUsuario;
        this.email = email; // Se establece una sola vez
        this.dni = dni;
        this.telefono = telefono;
        this.tipoUsuario = tipoUsuario;
        this.imageUrl = imageUrl;
        this.contrasena = contrasena;
    }

    // Getters y Setters
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getEmail() {
        return email;
    }

    // Eliminar el setter para el correo electrónico para que no sea editable
    // public void setEmail(String email) {
    //    this.email = email;
    // }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
