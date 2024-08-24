package com.ejercicio2.energysaver;

import android.app.Dialog;
import android.os.Bundle;

import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

public class MostrarUsoIndustriaFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_FECHA = "fecha";
    private static final String ARG_IMAGE_URL = "imageUrl";

    private String title;
    private String description;
    private String fecha;
    private String imageUrl;

    public MostrarUsoIndustriaFragment() {
        // Required empty public constructor
    }

    public static MostrarUsoIndustriaFragment newInstance(String title, String description, String fecha, String imageUrl) {
        MostrarUsoIndustriaFragment fragment = new MostrarUsoIndustriaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_FECHA, fecha);
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESCRIPTION);
            fecha = getArguments().getString(ARG_FECHA);
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Crear el diálogo
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.fragment_mostrar_uso_industria);
        dialog.setTitle("Detalle del Uso de Industria"); // Título del diálogo

        // Configurar la vista del diálogo
        View view = dialog.findViewById(android.R.id.content);
        TextView titleTextView = view.findViewById(R.id.title);
        TextView descriptionTextView = view.findViewById(R.id.description);
        TextView fechaTextView = view.findViewById(R.id.fecha);
        ImageView imageView = view.findViewById(R.id.image);
        Button btnCerrar = view.findViewById(R.id.btn_cerrar);

        titleTextView.setText(title);
        descriptionTextView.setText(description);
        fechaTextView.setText(fecha);

        Glide.with(this)
                .load(imageUrl)
                .into(imageView);

        // Configurar la acción de cerrar el diálogo
        btnCerrar.setOnClickListener(v -> dismiss());

        return dialog;
    }
}
