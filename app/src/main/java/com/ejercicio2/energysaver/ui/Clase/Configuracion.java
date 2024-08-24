package com.ejercicio2.energysaver.ui.Clase;

import android.os.Parcel;
import android.os.Parcelable;

public class Configuracion implements Parcelable {
    private String titulo;
    private String descripcion;
    private String imagenUrl;
    private String fecha;

    public Configuracion() {
        // Constructor vacío requerido para deserialización
    }

    public Configuracion(String titulo, String descripcion, String imagenUrl, String fecha) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.fecha = fecha;
    }

    protected Configuracion(Parcel in) {
        titulo = in.readString();
        descripcion = in.readString();
        imagenUrl = in.readString();
        fecha = in.readString();
    }

    public static final Parcelable.Creator<Configuracion> CREATOR = new Parcelable.Creator<Configuracion>() {
        @Override
        public Configuracion createFromParcel(Parcel in) {
            return new Configuracion(in);
        }

        @Override
        public Configuracion[] newArray(int size) {
            return new Configuracion[size];
        }
    };

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public String getFecha() {
        return fecha;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(titulo);
        parcel.writeString(descripcion);
        parcel.writeString(imagenUrl);
        parcel.writeString(fecha);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
