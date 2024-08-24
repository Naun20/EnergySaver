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


public class MostrarAhorroFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_FECHA = "fecha";
    private static final String ARG_IMAGE_URL = "imageUrl";

    private String title;
    private String description;
    private String fecha;
    private String imageUrl;

    public MostrarAhorroFragment() {
        // Constructor vacío requerido
    }

    public static MostrarAhorroFragment newInstance(String title, String description, String fecha, String imageUrl) {
        MostrarAhorroFragment fragment = new MostrarAhorroFragment();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mostrar_ahorro, container, false);

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
