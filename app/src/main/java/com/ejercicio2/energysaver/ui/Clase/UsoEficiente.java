package com.ejercicio2.energysaver.ui.Clase;

import android.os.Parcel;
import android.os.Parcelable;

public class UsoEficiente implements Parcelable {
    private String titulo;
    private String descripcion;
    private String imagenUrl;

    public UsoEficiente() {
        // Constructor vacío requerido para deserialización
    }

    public UsoEficiente(String titulo, String descripcion, String imagenUrl) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
    }

    protected UsoEficiente(Parcel in) {
        titulo = in.readString();
        descripcion = in.readString();
        imagenUrl = in.readString();
    }

    public static final Creator<UsoEficiente> CREATOR = new Creator<UsoEficiente>() {
        @Override
        public UsoEficiente createFromParcel(Parcel in) {
            return new UsoEficiente(in);
        }

        @Override
        public UsoEficiente[] newArray(int size) {
            return new UsoEficiente[size];
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

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(titulo);
        parcel.writeString(descripcion);
        parcel.writeString(imagenUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
