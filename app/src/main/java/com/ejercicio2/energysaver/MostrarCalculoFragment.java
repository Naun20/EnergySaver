package com.ejercicio2.energysaver;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;


public class MostrarCalculoFragment extends DialogFragment {

    private static final String ARG_TITULO = "titulo";
    private static final String ARG_DESCRIPCION = "descripcion";
    private static final String ARG_FECHA = "fecha";
    private static final String ARG_IMAGEN_URL = "imagenUrl";

    private String titulo;
    private String descripcion;
    private String fecha;
    private String imagenUrl;

    public MostrarCalculoFragment() {
        // Constructor vacío requerido
    }

    public static MostrarCalculoFragment newInstance(String titulo, String descripcion, String fecha, String imagenUrl) {
        MostrarCalculoFragment fragment = new MostrarCalculoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITULO, titulo);
        args.putString(ARG_DESCRIPCION, descripcion);
        args.putString(ARG_FECHA, fecha);
        args.putString(ARG_IMAGEN_URL, imagenUrl);
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
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mostrar_calculo, container, false);

        TextView tituloTextView = view.findViewById(R.id.title);
        TextView descripcionTextView = view.findViewById(R.id.description);
        TextView fechaTextView = view.findViewById(R.id.fecha);
        ImageView imagenView = view.findViewById(R.id.image);
        Button btnCerrar = view.findViewById(R.id.btn_cerrar);

        tituloTextView.setText(titulo);
        descripcionTextView.setText(descripcion);
        fechaTextView.setText(fecha);

        Glide.with(this)
                .load(imagenUrl)
                .into(imagenView);

        // Configurar la acción de cerrar el fragmento
        btnCerrar.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
    }
}
