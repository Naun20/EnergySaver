package com.ejercicio2.energysaver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

public class MostrarNoticiaFragment extends DialogFragment {

    private static final String ARG_TITULO = "titulo";
    private static final String ARG_DESCRIPCION = "descripcion";
    private static final String ARG_FECHA = "fecha";
    private static final String ARG_IMAGEN_URL = "imagenUrl";
    private static final String ARG_ENLACE = "enlace";

    private String titulo;
    private String descripcion;
    private String fecha;
    private String imagenUrl;
    private String enlace;

    public MostrarNoticiaFragment() {
        // Required empty public constructor
    }

    public static MostrarNoticiaFragment newInstance(String titulo, String descripcion, String fecha, String imagenUrl, String enlace) {
        MostrarNoticiaFragment fragment = new MostrarNoticiaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITULO, titulo);
        args.putString(ARG_DESCRIPCION, descripcion);
        args.putString(ARG_FECHA, fecha);
        args.putString(ARG_IMAGEN_URL, imagenUrl);
        args.putString(ARG_ENLACE, enlace);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            titulo = getArguments().getString(ARG_TITULO);
            descripcion = getArguments().getString(ARG_DESCRIPCION);
            fecha = getArguments().getString(ARG_FECHA);
            imagenUrl = getArguments().getString(ARG_IMAGEN_URL);
            enlace = getArguments().getString(ARG_ENLACE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mostrar_noticia, container, false);

        ImageView imagenNoticia = view.findViewById(R.id.imagen_noticia);
        TextView tituloNoticia = view.findViewById(R.id.titulo_noticia);
        TextView descripcionNoticia = view.findViewById(R.id.descripción_noticia);
        TextView fechaNoticia = view.findViewById(R.id.fechanoticia);
        Button btnEnlace = view.findViewById(R.id.btn_enlace);
        Button botonVolver = view.findViewById(R.id.boton_volver);

        Glide.with(this).load(imagenUrl).into(imagenNoticia);
        tituloNoticia.setText(titulo);
        descripcionNoticia.setText(descripcion);
        fechaNoticia.setText(fecha);

        btnEnlace.setOnClickListener(v -> {
            if (enlace != null && !enlace.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(enlace));
                startActivity(intent);
            }
        });

        botonVolver.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
