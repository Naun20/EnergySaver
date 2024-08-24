package com.ejercicio2.energysaver;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.ejercicio2.energysaver.ui.Adaptador.UsuarioAdapter;
import com.ejercicio2.energysaver.ui.Clase.Usuario;

public class UsuariosFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsuarioAdapter usuarioAdapter;
    private FirebaseFirestore mFirestore;
    private Query query;
    private Button btnAddUsuario;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        query = mFirestore.collection("usuarios"); // Cambia a la colección que tengas en Firestore
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_usuarios, container, false);

        // Inicializa el RecyclerView
        recyclerView = root.findViewById(R.id.recycleViewUsuario);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configura el adaptador
        FirestoreRecyclerOptions<Usuario> options = new FirestoreRecyclerOptions.Builder<Usuario>()
                .setQuery(query, Usuario.class)
                .build();

        usuarioAdapter = new UsuarioAdapter(options, getContext());
        recyclerView.setAdapter(usuarioAdapter);

        // Inicializa el botón y configura el listener
        btnAddUsuario = root.findViewById(R.id.btn_addUsuario);
        btnAddUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lanzar la actividad CreateUsuarioActivity
                Intent intent = new Intent(getActivity(), CreateUsuarioActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (usuarioAdapter != null) {
            usuarioAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (usuarioAdapter != null) {
           // usuarioAdapter.stopListening();
        }
    }
}
