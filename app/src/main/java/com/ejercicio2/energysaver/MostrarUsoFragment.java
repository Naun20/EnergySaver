package com.ejercicio2.energysaver;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

public class MostrarUsoFragment extends DialogFragment {

    private static final String ARG_IMAGEN_URL = "imagenUrl";
    private static final String ARG_TITULO = "titulo";
    private static final String ARG_DESCRIPCION = "descripcion";

    private String imagenUrl;
    private String titulo;
    private String descripcion;

    public static MostrarUsoFragment newInstance(String imagenUrl, String titulo, String descripcion) {
        MostrarUsoFragment fragment = new MostrarUsoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGEN_URL, imagenUrl);
        args.putString(ARG_TITULO, titulo);
        args.putString(ARG_DESCRIPCION, descripcion);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imagenUrl = getArguments().getString(ARG_IMAGEN_URL);
            titulo = getArguments().getString(ARG_TITULO);
            descripcion = getArguments().getString(ARG_DESCRIPCION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mostrar_uso, container, false);

        ImageView imageView = view.findViewById(R.id.uso_image);
        TextView titleTextView = view.findViewById(R.id.uso_title);
        TextView descriptionTextView = view.findViewById(R.id.uso_description);
        Button closeButton = view.findViewById(R.id.close_button);

        Glide.with(this).load(imagenUrl).into(imageView);
        titleTextView.setText(titulo);
        descriptionTextView.setText(descripcion);

        // Añade funcionalidad al botón de cerrar
        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Ajusta el tamaño del DialogFragment para que ocupe toda la pantalla
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
