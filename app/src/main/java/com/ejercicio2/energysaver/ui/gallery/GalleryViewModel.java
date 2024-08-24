package com.ejercicio2.energysaver.ui.gallery;

import androidx.lifecycle.ViewModel;

import com.ejercicio2.energysaver.ui.Clase.Calculo;
import com.ejercicio2.energysaver.ui.Clase.Proyecto;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class GalleryViewModel extends ViewModel {

    private FirestoreRecyclerOptions<Proyecto> proyectoOptions;
    private FirestoreRecyclerOptions<Calculo> calculoOptions;
    private FirebaseFirestore db;

    public GalleryViewModel() {
        db = FirebaseFirestore.getInstance();
        loadProyectos();
        loadCalculos();
    }

    public FirestoreRecyclerOptions<Proyecto> getProyectoOptions() {
        return proyectoOptions;
    }

    public FirestoreRecyclerOptions<Calculo> getCalculoOptions() {
        return calculoOptions;
    }

    private void loadProyectos() {
        Query query = db.collection("proyectos").orderBy("fecha", Query.Direction.DESCENDING);

        proyectoOptions = new FirestoreRecyclerOptions.Builder<Proyecto>()
                .setQuery(query, Proyecto.class)
                .build();
    }

    private void loadCalculos() {
        Query query = db.collection("calculos").orderBy("fecha", Query.Direction.DESCENDING);

        calculoOptions = new FirestoreRecyclerOptions.Builder<Calculo>()
                .setQuery(query, Calculo.class)
                .build();
    }
}
