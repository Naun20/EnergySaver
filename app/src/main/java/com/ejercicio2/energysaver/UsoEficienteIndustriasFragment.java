package com.ejercicio2.energysaver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ejercicio2.energysaver.ui.Adaptador.IndustriaEficienciaAdapter;
import com.ejercicio2.energysaver.ui.Clase.IndustriaEficiencia;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UsoEficienteIndustriasFragment extends Fragment {

    private static final int CREATE_USOINDUSTRIAS_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private IndustriaEficienciaAdapter industriaEficienciaAdapter;
    private FirebaseFirestore mFirestore;
    private Query query;
    private FirebaseAuth auth;


    public UsoEficienteIndustriasFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();  // Inicializa FirebaseAuth
        query = mFirestore.collection("uso_industrias");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_uso_eficiente_industrias, container, false);

        // Inicializa el RecyclerView
        recyclerView = root.findViewById(R.id.recycleViewUsoIndustria);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configura el adaptador
        configureAdapter();

         // Configura el FloatingActionButton
        FloatingActionButton fabAdd = root.findViewById(R.id.addusoindustria);

        // Verifica si el usuario está autenticado por correo y muestra/oculta el FAB
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            fabAdd.setVisibility(View.VISIBLE);
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        // Configura el click listener del FAB
        fabAdd.setOnClickListener(v -> mostrarDialogoSeleccion());

        return root;
    }

    private void configureAdapter() {
        FirestoreRecyclerOptions<IndustriaEficiencia> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<IndustriaEficiencia>()
                        .setQuery(query, IndustriaEficiencia.class)
                        .build();

        industriaEficienciaAdapter = new IndustriaEficienciaAdapter(firestoreRecyclerOptions, getContext());
        recyclerView.setAdapter(industriaEficienciaAdapter);
    }

    private void mostrarDialogoSeleccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar una opción")
                .setItems(new CharSequence[]{"Agregar Uso Eficiente"}, (dialog, which) -> {
                    if (which == 0) {
                        startCreateIndustriaActivity();
                    }
                });
        builder.create().show();
    }

    private void startCreateIndustriaActivity() {
        Intent intent = new Intent(getContext(), CreateUsoIndustriasActivity.class);
        startActivityForResult(intent, CREATE_USOINDUSTRIAS_REQUEST_CODE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (industriaEficienciaAdapter != null) {
            industriaEficienciaAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (industriaEficienciaAdapter != null) {
            // industriaEficienciaAdapter.stopListening();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_USOINDUSTRIAS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Actualiza los datos en el adaptador
            if (industriaEficienciaAdapter != null) {
                industriaEficienciaAdapter.notifyDataSetChanged();
            }
        }
    }
}
