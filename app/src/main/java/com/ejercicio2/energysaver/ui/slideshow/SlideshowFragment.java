package com.ejercicio2.energysaver.ui.slideshow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ejercicio2.energysaver.ConfiguracionesActivity;
import com.ejercicio2.energysaver.CreateConsejoActivity;
import com.ejercicio2.energysaver.CreateConsejoFragment;
import com.ejercicio2.energysaver.GuiaActivity;
import com.ejercicio2.energysaver.databinding.FragmentSlideshowBinding;
import com.ejercicio2.energysaver.ui.Adaptador.ConfiguracionAdapter;
import com.ejercicio2.energysaver.ui.Adaptador.UsoEficienteCompletoAdapter;
import com.ejercicio2.energysaver.ui.Clase.Configuracion;
import com.ejercicio2.energysaver.ui.Clase.UsoEficiente;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private SlideshowViewModel slideshowViewModel;
    private UsoEficienteCompletoAdapter usoEficienteCompletoAdapter;
    private ConsejoAdapter consejoAdapter;
    private ConfiguracionAdapter configuracionAdapter;
    private Handler handler;
    private Runnable autoScrollRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerViewConsejos = binding.recyclerViewConsejos;
        RecyclerView recyclerViewUsoEficiente = binding.recyclerViewGuias;
        RecyclerView recyclerViewConfiguraciones = binding.recyclerViewConfiguraciones; // Nuevo RecyclerView para configuraciones

        // Configuración del RecyclerView
        recyclerViewConsejos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewUsoEficiente.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewConfiguraciones.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)); // Configuración vertical para el RecyclerView de configuraciones

        // Observador para los consejos
        slideshowViewModel.getConsejos().observe(getViewLifecycleOwner(), new Observer<List<Consejo>>() {
            @Override
            public void onChanged(List<Consejo> consejoList) {
                consejoAdapter = new ConsejoAdapter(getContext(), consejoList);
                recyclerViewConsejos.setAdapter(consejoAdapter);
            }
        });

        // Observador para el uso eficiente
        slideshowViewModel.getUsoEficiente().observe(getViewLifecycleOwner(), new Observer<List<UsoEficiente>>() {
            @Override
            public void onChanged(List<UsoEficiente> usoEficienteList) {
                usoEficienteCompletoAdapter = new UsoEficienteCompletoAdapter(getContext(), usoEficienteList);
                recyclerViewUsoEficiente.setAdapter(usoEficienteCompletoAdapter);
                startAutoScroll(recyclerViewUsoEficiente);
            }
        });

        // Observador para las configuraciones
        slideshowViewModel.getConfiguraciones().observe(getViewLifecycleOwner(), new Observer<List<Configuracion>>() {
            @Override
            public void onChanged(List<Configuracion> configuracionList) {
                configuracionAdapter = new ConfiguracionAdapter(getContext(), configuracionList);
                recyclerViewConfiguraciones.setAdapter(configuracionAdapter);
            }
        });

        // Configuración del FloatingActionButton
        FloatingActionButton fabAddConsejo = binding.addconsejo;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        // Mostrar/Ocultar FAB según el estado de autenticación
        fabAddConsejo.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);

        fabAddConsejo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoSeleccion();
            }
        });

        return root;
    }

    private void mostrarDialogoSeleccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar una opción")
                .setItems(new CharSequence[]{"Agregar Consejo", "Agregar Uso Eficiente", "Agregar Configuración"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showCreateConsejoDialog();
                            break;
                        case 1:
                            startGuiaActivity();
                            break;
                        case 2:
                            startConfiguracionesActivity();
                            break;
                    }
                });
        builder.create().show();
    }

    private void startGuiaActivity() {
        Intent intent = new Intent(getContext(), GuiaActivity.class);
        startActivity(intent);
    }

    private void showCreateConsejoDialog() {
        Intent intent = new Intent(getContext(), CreateConsejoActivity.class);
        startActivity(intent);
    }


    private void startConfiguracionesActivity() {
        Intent intent = new Intent(getContext(), ConfiguracionesActivity.class);
        startActivity(intent);
    }

    private void startAutoScroll(RecyclerView recyclerView) {
        handler = new Handler();
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (recyclerView.getLayoutManager() != null) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    int totalItemCount = layoutManager.getItemCount();
                    if (lastVisibleItemPosition < (totalItemCount - 1)) {
                        recyclerView.smoothScrollToPosition(lastVisibleItemPosition + 1);
                    } else {
                        recyclerView.smoothScrollToPosition(0);
                    }
                }
                handler.postDelayed(this, 8000); // Desplazar cada 8 segundos
            }
        };
        handler.postDelayed(autoScrollRunnable, 8000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && autoScrollRunnable != null) {
            handler.removeCallbacks(autoScrollRunnable);
        }
        binding = null;
    }
}
