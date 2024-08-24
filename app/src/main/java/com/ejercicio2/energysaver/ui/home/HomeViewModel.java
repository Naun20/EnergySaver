package com.ejercicio2.energysaver.ui.home;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<Novedad>> novedades;
    private MutableLiveData<List<Noticia>> noticias;
    private FirebaseFirestore db;

    public HomeViewModel() {
        novedades = new MutableLiveData<>();
        noticias = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();
        loadNovedades();
        loadNoticias();
    }

    public LiveData<List<Novedad>> getNovedades() {
        return novedades;
    }

    public LiveData<List<Noticia>> getNoticias() {
        return noticias;
    }

    private void loadNovedades() {
        db.collection("novedades")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            // Manejar el error
                            return;
                        }

                        List<Novedad> listaNovedades = new ArrayList<>();
                        for (DocumentSnapshot doc : value) {
                            Novedad novedad = doc.toObject(Novedad.class);
                            listaNovedades.add(novedad);
                        }
                        novedades.setValue(listaNovedades);
                    }
                });
    }

    private void loadNoticias() {
        db.collection("noticias")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            // Manejar el error
                            return;
                        }

                        List<Noticia> listaNoticias = new ArrayList<>();
                        for (DocumentSnapshot doc : value) {
                            Noticia noticia = doc.toObject(Noticia.class);
                            listaNoticias.add(noticia);
                        }
                        noticias.setValue(listaNoticias);
                    }
                });
    }
}
