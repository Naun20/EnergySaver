package com.ejercicio2.energysaver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ejercicio2.energysaver.ui.Adaptador.EficienciaIndustriaAdaptador;
import com.ejercicio2.energysaver.ui.Clase.EficienciaIndustrias;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import android.app.AlertDialog;

public class EficienciaindustriaFragment extends Fragment {

    private static final int CREATE_EFICIENCIA_REQUEST_CODE = 1;

    private RecyclerView recyclerViewEficiencia;
    private EficienciaIndustriaAdaptador eficienciaIndustriaAdaptador;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth auth;

    public EficienciaindustriaFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_eficienciaindustria, container, false);

        // Configurar RecyclerView para Eficiencia Industria
        recyclerViewEficiencia = root.findViewById(R.id.recyclerViewEficienciaIndustria);
        recyclerViewEficiencia.setLayoutManager(new LinearLayoutManager(getContext()));
        configureEficienciaAdapter();

        // Configurar FloatingActionButton
        FloatingActionButton fabAdd = root.findViewById(R.id.addEficienciaIndustria);
        configureFloatingActionButton(fabAdd);

        return root;
    }

    private void configureEficienciaAdapter() {
        Query eficienciaQuery = mFirestore.collection("eficiencia_industrias");

        FirestoreRecyclerOptions<EficienciaIndustrias> options =
                new FirestoreRecyclerOptions.Builder<EficienciaIndustrias>()
                        .setQuery(eficienciaQuery, EficienciaIndustrias.class)
                        .build();

        eficienciaIndustriaAdaptador = new EficienciaIndustriaAdaptador(options, getContext());
        recyclerViewEficiencia.setAdapter(eficienciaIndustriaAdaptador);
    }

    private void configureFloatingActionButton(FloatingActionButton fabAdd) {
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            fabAdd.setVisibility(View.VISIBLE);
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        fabAdd.setOnClickListener(v -> mostrarDialogoSeleccion());
    }

    private void mostrarDialogoSeleccion() {
        new AlertDialog.Builder(getContext())
                .setTitle("Seleccionar una opción")
                .setItems(new CharSequence[]{"Agregar Eficiencia Industria"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            startCreateEficienciaIndustriaActivity();
                            break;
                    }
                })
                .create()
                .show();
    }

    private void startCreateEficienciaIndustriaActivity() {
        Intent intent = new Intent(getContext(), CreateEficienciaIndustriaActivity.class);
        startActivityForResult(intent, CREATE_EFICIENCIA_REQUEST_CODE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (eficienciaIndustriaAdaptador != null) {
            eficienciaIndustriaAdaptador.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (eficienciaIndustriaAdaptador != null) {
           // eficienciaIndustriaAdaptador.stopListening();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // Handle results if needed
        }
    }
}
