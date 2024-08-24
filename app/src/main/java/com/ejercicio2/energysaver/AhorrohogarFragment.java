package com.ejercicio2.energysaver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ejercicio2.energysaver.ui.Adaptador.AhorroHogarAdapter;
import com.ejercicio2.energysaver.ui.Clase.AhorroHogar;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AhorrohogarFragment extends Fragment {

    private static final int CREATE_AHORROHOGAR_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private AhorroHogarAdapter ahorroHogarAdapter;
    private FirebaseFirestore mFirestore;
    private Query query;
    private FirebaseAuth auth;

    public AhorrohogarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();  // Inicializa FirebaseAuth
        query = mFirestore.collection("ahorro_hogar"); // Suponiendo que "ahorro_hogar" es tu colección en Firestore
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ahorrohogar, container, false);

        // Inicializa el RecyclerView
        recyclerView = root.findViewById(R.id.recyclerViewAhorroHogar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configura el adaptador
        configureAdapter();

        // Configura el FloatingActionButton
        FloatingActionButton fabAdd = root.findViewById(R.id.addAhorroHogar);

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
        FirestoreRecyclerOptions<AhorroHogar> firestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<AhorroHogar>()
                        .setQuery(query, AhorroHogar.class)
                        .build();

        ahorroHogarAdapter = new AhorroHogarAdapter(firestoreRecyclerOptions, getContext());
        recyclerView.setAdapter(ahorroHogarAdapter);
    }

    private void mostrarDialogoSeleccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar una opción")
                .setItems(new CharSequence[]{"Agregar Ahorro Hogar"}, (dialog, which) -> {
                    if (which == 0) {
                        startCreateAhorroHogarActivity();
                    }
                });
        builder.create().show();
    }

    private void startCreateAhorroHogarActivity() {
        Intent intent = new Intent(getContext(), CreateAhorroHogarActivity.class);
        startActivityForResult(intent, CREATE_AHORROHOGAR_REQUEST_CODE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (ahorroHogarAdapter != null) {
            ahorroHogarAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (ahorroHogarAdapter != null) {
          //  ahorroHogarAdapter.stopListening();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_AHORROHOGAR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Actualiza los datos en el adaptador
            if (ahorroHogarAdapter != null) {
                ahorroHogarAdapter.notifyDataSetChanged();
            }
        }
    }
}
