package com.ejercicio2.energysaver;


import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

public class MostrarConsejoFragment extends DialogFragment {

    private static final String ARG_IMAGE_URL = "imageUrl";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_DATE = "date";

    private String imageUrl;
    private String title;
    private String description;
    private String date;

    public MostrarConsejoFragment() {
        // Required empty public constructor
    }

    public static MostrarConsejoFragment newInstance(String imageUrl, String title, String description, String date) {
        MostrarConsejoFragment fragment = new MostrarConsejoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Establecer el estilo del diálogo para que tenga un fondo transparente
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
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
        View view = inflater.inflate(R.layout.fragment_mostrar_consejo, container, false);

        ImageView imgConsejo = view.findViewById(R.id.img_consejo);
        TextView txtTitleConsejo = view.findViewById(R.id.txt_title_consejo);
        TextView txtDescriptionConsejo = view.findViewById(R.id.txt_description_consejo);
        TextView txtDateConsejo = view.findViewById(R.id.txt_date_consejo);
        Button btnCerrar = view.findViewById(R.id.btn_cerrar);

        // Cargar la imagen con Glide
        Glide.with(this).load(imageUrl).into(imgConsejo);

        txtTitleConsejo.setText(title);
        txtDescriptionConsejo.setText(description);
        txtDateConsejo.setText(date);

        // Configurar el botón para cerrar el diálogo
        btnCerrar.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ajustar el tamaño del diálogo
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                Display display = window.getWindowManager().getDefaultDisplay();
                layoutParams.copyFrom(window.getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Usar el ancho del parent
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Ajustar la altura del diálogo
                window.setAttributes(layoutParams);
            }
        }
    }
}
