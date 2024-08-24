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

public class MostrarEficienciaEmpresaFragmentFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_FECHA = "fecha";
    private static final String ARG_IMAGE_URL = "imageUrl";

    private String title;
    private String description;
    private String fecha;
    private String imageUrl;

    public MostrarEficienciaEmpresaFragmentFragment() {
        // Required empty public constructor
    }

    public static MostrarEficienciaEmpresaFragmentFragment newInstance(String title, String description, String fecha, String imageUrl) {
        MostrarEficienciaEmpresaFragmentFragment fragment = new MostrarEficienciaEmpresaFragmentFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_mostrar_eficiencia_empresa_fragment, container, false);

        // Configurar la vista del fragmento
        TextView titleTextView = view.findViewById(R.id.titulo);
        TextView descriptionTextView = view.findViewById(R.id.descripcion);
        TextView fechaTextView = view.findViewById(R.id.fecha);
        ImageView imageView = view.findViewById(R.id.imagen);
        Button btnCerrar = view.findViewById(R.id.btn_cerrar);

        titleTextView.setText(title);
        descriptionTextView.setText(description);
        fechaTextView.setText(fecha);

        Glide.with(this)
                .load(imageUrl)
                .into(imageView);

        // Customize Toolbar...
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cierra el fragmento actual
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction().remove(MostrarEficienciaEmpresaFragmentFragment.this).commit();
                }
            }
        });


        return view;
    }
}