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

public class MostrarUsoEmpresaFragment extends Fragment {

    private static final String ARG_IMAGE_URL = "imageUrl";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_DATE = "date";

    private String imageUrl;
    private String title;
    private String description;
    private String date;

    public MostrarUsoEmpresaFragment() {
        // Constructor público vacío
    }

    public static MostrarUsoEmpresaFragment newInstance(String imageUrl, String title, String description, String date) {
        MostrarUsoEmpresaFragment fragment = new MostrarUsoEmpresaFragment();
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
        View view = inflater.inflate(R.layout.fragment_mostrar_uso_empresa, container, false);

        ImageView imgProyecto = view.findViewById(R.id.imagen);
        TextView txtTitleProyecto = view.findViewById(R.id.titulo);
        TextView txtDescriptionProyecto = view.findViewById(R.id.descripcion);
        TextView txtDateProyecto = view.findViewById(R.id.fecha);
        Button btnCerrar = view.findViewById(R.id.btn_cerrar);

        // Cargar la imagen con Glide
        Glide.with(this).load(imageUrl).into(imgProyecto);

        txtTitleProyecto.setText(title);
        txtDescriptionProyecto.setText(description);
        txtDateProyecto.setText(date);

        // Agregar el evento de clic al botón cerrar
        btnCerrar.setOnClickListener(v -> {
            // Cierra el fragmento actual
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(MostrarUsoEmpresaFragment.this).commit();
            }
        });

        return view;
    }
}
