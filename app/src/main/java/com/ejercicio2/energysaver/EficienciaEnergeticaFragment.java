package com.ejercicio2.energysaver;

import android.app.Activity;

import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ejercicio2.energysaver.ui.Adaptador.EficienciaAdapter;
import com.ejercicio2.energysaver.ui.Clase.Eficiencia;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class EficienciaEnergeticaFragment extends Fragment {

    private static final int CREATE_EFICIENCIA_REQUEST_CODE = 1; // Código para identificar la actividad de creación

    private RecyclerView recyclerView;
    private EficienciaAdapter eficienciaAdapter;
    private FirebaseFirestore mFirestore;
    private Query query;
    private FirebaseAuth auth;

    public EficienciaEnergeticaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();  // Inicializa FirebaseAuth
        query = mFirestore.collection("eficiencia_energetica"); // Suponiendo que "eficiencia_energetica" es tu colección en Firestore
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_eficiencia_energetica, container, false);

        // Inicializa el RecyclerView
        recyclerView = root.findViewById(R.id.recyclerViewEficiencia);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView textViewSubtema2 = root.findViewById(R.id.textViewSubtema2);
        textViewSubtema2.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.rapidtables.org/calc/electric/energy-consumption-calculator.html"));
            startActivity(intent);
        });

        // Configura el adaptador
        configureAdapter();

        // Configura el FloatingActionButton
        FloatingActionButton fabAdd = root.findViewById(R.id.addeficiencia);

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
        FirestoreRecyclerOptions<Eficiencia> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<Eficiencia>()
                        .setQuery(query, Eficiencia.class)
                        .build();

        eficienciaAdapter = new EficienciaAdapter(firestoreRecyclerOptions, getContext());
        recyclerView.setAdapter(eficienciaAdapter);
    }

    private void mostrarDialogoSeleccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar una opción")
                .setItems(new CharSequence[]{"Agregar Eficiencia"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            startCreateEficienciaActivity();
                            break;
                    }
                });
        builder.create().show();
    }

    private void startCreateEficienciaActivity() {
        Intent intent = new Intent(getContext(), CreateEficienciaActivity.class);
        startActivityForResult(intent, CREATE_EFICIENCIA_REQUEST_CODE); // Usamos startActivityForResult para recibir el resultado
    }

    @Override
    public void onStart() {
        super.onStart();
        if (eficienciaAdapter != null) {
            eficienciaAdapter.startListening(); // Inicia la escucha del adaptador
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (eficienciaAdapter != null) {
            //eficienciaAdapter.stopListening(); // Detiene la escucha del adaptador
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_EFICIENCIA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Aquí actualizas el adaptador o recargas los datos
            eficienciaAdapter.notifyDataSetChanged();
        }
    }
}
