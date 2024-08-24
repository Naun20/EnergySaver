package com.ejercicio2.energysaver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class MostrarEficienciaIndustriaFragment extends Fragment {

    private static final String ARG_IMAGE_URL = "imageUrl";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_DATE = "date";

    private String imageUrl;
    private String title;
    private String description;
    private String date;

    public MostrarEficienciaIndustriaFragment() {
        // Required empty public constructor
    }

    public static MostrarEficienciaIndustriaFragment newInstance(String imageUrl, String title, String description, String date) {
        MostrarEficienciaIndustriaFragment fragment = new MostrarEficienciaIndustriaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESCRIPTION);
            date = getArguments().getString(ARG_DATE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mostrar_eficiencia_industria, container, false);

        ImageView imgConsejo = view.findViewById(R.id.imagen);
        TextView txtTitleConsejo = view.findViewById(R.id.titulo);
        TextView txtDescriptionConsejo = view.findViewById(R.id.descripcion);
        TextView txtDateConsejo = view.findViewById(R.id.fecha);
        Button btnCerrar = view.findViewById(R.id.btn_cerrar);

        // Cargar la imagen con Glide
        Glide.with(this).load(imageUrl).into(imgConsejo);

        txtTitleConsejo.setText(title);
        txtDescriptionConsejo.setText(description);
        txtDateConsejo.setText(date);

        // Agregar el evento de clic al botón cerrar
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cierra el fragmento actual
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction().remove(MostrarEficienciaIndustriaFragment.this).commit();
                }
            }
        });

        return view;
    }
}
