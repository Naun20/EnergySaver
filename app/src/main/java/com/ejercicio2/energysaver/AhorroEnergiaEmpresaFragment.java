package com.ejercicio2.energysaver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ejercicio2.energysaver.ui.Adaptador.AhorroEmpresaAdapter;
import com.ejercicio2.energysaver.ui.Clase.AhorroEmpresa;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AhorroEnergiaEmpresaFragment extends Fragment {

    private static final int CREATE_AHORROEMPRESA_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private AhorroEmpresaAdapter ahorroEmpresaAdapter;
    private FirebaseFirestore mFirestore;
    private Query query;
    private FirebaseAuth auth;

    public AhorroEnergiaEmpresaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        query = mFirestore.collection("ahorro_empresa"); // Suponiendo que "ahorro_empresa" es tu colección en Firestore
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ahorro_energia_empresa, container, false);

        // Inicializa el RecyclerView
        recyclerView = root.findViewById(R.id.recyclerViewAhorroEmpresa);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configura el adaptador
        configureAdapter();

        // Configura el FloatingActionButton
        FloatingActionButton fabAdd = root.findViewById(R.id.addAhorroEmpresa);

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
        FirestoreRecyclerOptions<AhorroEmpresa> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<AhorroEmpresa>()
                        .setQuery(query, AhorroEmpresa.class)
                        .build();

        ahorroEmpresaAdapter = new AhorroEmpresaAdapter(firestoreRecyclerOptions, getContext());
        recyclerView.setAdapter(ahorroEmpresaAdapter);
    }

    private void mostrarDialogoSeleccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar una opción")
                .setItems(new CharSequence[]{"Agregar Ahorro Empresa"}, (dialog, which) -> {
                    if (which == 0) {
                        startCreateAhorroEmpresaActivity();
                    }
                });
        builder.create().show();
    }

    private void startCreateAhorroEmpresaActivity() {
        Intent intent = new Intent(getContext(), CreateAhorroEmpresaActivity.class);
        startActivityForResult(intent, CREATE_AHORROEMPRESA_REQUEST_CODE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (ahorroEmpresaAdapter != null) {
            ahorroEmpresaAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (ahorroEmpresaAdapter != null) {
         //   ahorroEmpresaAdapter.stopListening();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_AHORROEMPRESA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Actualiza los datos en el adaptador
            if (ahorroEmpresaAdapter != null) {
                ahorroEmpresaAdapter.notifyDataSetChanged();
            }
        }
    }
}
