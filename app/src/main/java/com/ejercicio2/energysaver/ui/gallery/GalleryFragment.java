package com.ejercicio2.energysaver.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.ejercicio2.energysaver.ConfiguracionesActivity;
import com.ejercicio2.energysaver.CreateCalculoActivity;
import com.ejercicio2.energysaver.CreateProyectoActivity;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.databinding.FragmentGalleryBinding;
import com.ejercicio2.energysaver.ui.Adaptador.CalculoAdapter;
import com.ejercicio2.energysaver.ui.Adaptador.ProyectoAdapter;
import com.ejercicio2.energysaver.ui.Clase.Calculo;
import com.ejercicio2.energysaver.ui.Clase.Proyecto;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private ProyectoAdapter proyectoAdapter;
    private CalculoAdapter calculoAdapter;
    private GalleryViewModel galleryViewModel;
    private Handler autoScrollHandler;
    private int scrollPosition = 0;
    private static final String TAG = "GalleryFragment";
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        auth = FirebaseAuth.getInstance();  // Inicializa FirebaseAuth
        db = FirebaseFirestore.getInstance();

        galleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        // Configuración del RecyclerView para proyectos
        RecyclerView recyclerViewProyectos = binding.recyclerProyectos;
        LinearLayoutManager proyectoLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewProyectos.setLayoutManager(proyectoLayoutManager);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerViewProyectos);

        FirestoreRecyclerOptions<Proyecto> proyectoOptions = galleryViewModel.getProyectoOptions();
        proyectoAdapter = new ProyectoAdapter(proyectoOptions, getContext());
        recyclerViewProyectos.setAdapter(proyectoAdapter);

        // Configuración del RecyclerView para cálculos
        RecyclerView recyclerViewCalculos = binding.recyclerCalculos;
        LinearLayoutManager calculoLayoutManager = new LinearLayoutManager(getContext());
        recyclerViewCalculos.setLayoutManager(calculoLayoutManager);

        FirestoreRecyclerOptions<Calculo> calculoOptions = galleryViewModel.getCalculoOptions();
        calculoAdapter = new CalculoAdapter(calculoOptions, getContext());
        recyclerViewCalculos.setAdapter(calculoAdapter);

        // Configuración del Handler para desplazamiento automático
        autoScrollHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (recyclerViewProyectos.getAdapter() != null) {
                    int itemCount = recyclerViewProyectos.getAdapter().getItemCount();
                    if (itemCount > 0) {
                        scrollPosition = (scrollPosition + 1) % itemCount;
                        recyclerViewProyectos.smoothScrollToPosition(scrollPosition);
                    }
                }
                sendEmptyMessageDelayed(0, 5000); // 5000ms = 5 segundos
            }
        };

        // Configura el FloatingActionButton
        FloatingActionButton fabAdd = root.findViewById(R.id.addProyecto);

        // Verifica si el usuario está autenticado por correo y muestra/oculta el FAB
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            fabAdd.setVisibility(View.VISIBLE);
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        fabAdd.setOnClickListener(v -> mostrarDialogoSeleccion());

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (proyectoAdapter != null) {
            proyectoAdapter.startListening();
        }
        if (calculoAdapter != null) {
            calculoAdapter.startListening();
        }
        autoScrollHandler.sendEmptyMessageDelayed(0, 5000); // Inicia el desplazamiento automático
    }

    @Override
    public void onStop() {
        super.onStop();
        if (proyectoAdapter != null) {
           // proyectoAdapter.stopListening();
        }
        if (calculoAdapter != null) {
          //  calculoAdapter.stopListening();
        }
        autoScrollHandler.removeCallbacksAndMessages(null); // Detiene el desplazamiento automático
    }

    private void mostrarDialogoSeleccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar una opción")
                .setItems(new CharSequence[]{"Agregar Proyecto", "Agregar Calculo"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            startCreateProyectoActivity();
                            break;
                        case 1:
                            startConfiguracionesActivity();
                            break;
                    }
                });
        builder.create().show();
    }

    private void startCreateProyectoActivity() {
        Intent intent = new Intent(getContext(), CreateProyectoActivity.class);
        startActivity(intent);
    }

    private void startConfiguracionesActivity() {
        Intent intent = new Intent(getContext(), CreateCalculoActivity.class);
        startActivity(intent);
    }
}
