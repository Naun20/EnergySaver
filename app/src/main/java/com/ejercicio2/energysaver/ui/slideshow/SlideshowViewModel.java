package com.ejercicio2.energysaver.ui.slideshow;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ejercicio2.energysaver.ui.Clase.Configuracion;
import com.ejercicio2.energysaver.ui.Clase.UsoEficiente;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SlideshowViewModel extends ViewModel {

    private MutableLiveData<List<Consejo>> consejos;
    private MutableLiveData<List<UsoEficiente>> usoEficiente;
    private MutableLiveData<List<Configuracion>> configuraciones;
    private FirebaseFirestore db;

    public SlideshowViewModel() {
        consejos = new MutableLiveData<>();
        usoEficiente = new MutableLiveData<>();
        configuraciones = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();
        loadConsejos();
        loadUsoEficiente();
        loadConfiguraciones();
    }

    public LiveData<List<Consejo>> getConsejos() {
        return consejos;
    }

    public LiveData<List<UsoEficiente>> getUsoEficiente() {
        return usoEficiente;
    }

    public LiveData<List<Configuracion>> getConfiguraciones() {
        return configuraciones;
    }

    private void loadConsejos() {
        db.collection("consejos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            // Manejar el error
                            return;
                        }

                        List<Consejo> listaConsejos = new ArrayList<>();
                        if (value != null) {
                            for (DocumentSnapshot doc : value) {
                                Consejo consejo = doc.toObject(Consejo.class);
                                if (consejo != null) {
                                    listaConsejos.add(consejo);
                                }
                            }
                        }
                        consejos.setValue(listaConsejos);
                    }
                });
    }

    private void loadUsoEficiente() {
        db.collection("uso_eficiente")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            // Manejar el error
                            return;
                        }

                        List<UsoEficiente> listaUsoEficiente = new ArrayList<>();
                        if (value != null) {
                            for (DocumentSnapshot doc : value) {
                                UsoEficiente usoEficiente = doc.toObject(UsoEficiente.class);
                                if (usoEficiente != null) {
                                    listaUsoEficiente.add(usoEficiente);
                                }
                            }
                        }
                        usoEficiente.setValue(listaUsoEficiente);
                    }
                });
    }

    private void loadConfiguraciones() {
        db.collection("configuraciones_inteligentes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            // Manejar el error
                            return;
                        }

                        List<Configuracion> listaConfiguraciones = new ArrayList<>();
                        if (value != null) {
                            for (DocumentSnapshot doc : value) {
                                Configuracion configuracion = doc.toObject(Configuracion.class);
                                if (configuracion != null) {
                                    listaConfiguraciones.add(configuracion);
                                }
                            }
                        }
                        configuraciones.setValue(listaConfiguraciones);
                    }
                });
    }
}
