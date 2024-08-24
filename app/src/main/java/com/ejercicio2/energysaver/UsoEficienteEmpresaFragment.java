package com.ejercicio2.energysaver;

import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;

import com.ejercicio2.energysaver.ui.Adaptador.UsoEmpresaAdapter;
import com.ejercicio2.energysaver.ui.Adaptador.EficienciaEmpresaAdapter;
import com.ejercicio2.energysaver.ui.Clase.UsoEmpresa;
import com.ejercicio2.energysaver.ui.Clase.EficienciaEmpresa;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UsoEficienteEmpresaFragment extends Fragment {

    private static final int CREATE_USO_EMPRESA_REQUEST_CODE = 1;
    private static final int SCROLL_MESSAGE = 0;

    private RecyclerView recyclerViewUsoEmpresa;
    private RecyclerView recyclerViewEficienciaEmpresa;
    private UsoEmpresaAdapter usoEmpresaAdapter;
    private EficienciaEmpresaAdapter eficienciaEmpresaAdapter;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth auth;

    private LinearLayoutManager horizontalLayoutManagerUso;
    private LinearLayoutManager horizontalLayoutManagerEficiencia;

    private final Handler scrollHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == SCROLL_MESSAGE) {
                if (horizontalLayoutManagerUso != null && usoEmpresaAdapter != null) {
                    int lastVisibleItemPosition = horizontalLayoutManagerUso.findLastVisibleItemPosition();
                    int totalItemCount = usoEmpresaAdapter.getItemCount();

                    if (lastVisibleItemPosition >= totalItemCount - 1) {
                        recyclerViewUsoEmpresa.smoothScrollToPosition(0);
                    } else {
                        recyclerViewUsoEmpresa.smoothScrollToPosition(lastVisibleItemPosition + 1);
                    }
                    sendEmptyMessageDelayed(SCROLL_MESSAGE, 5000);
                }

                if (horizontalLayoutManagerEficiencia != null && eficienciaEmpresaAdapter != null) {
                    int lastVisibleItemPosition = horizontalLayoutManagerEficiencia.findLastVisibleItemPosition();
                    int totalItemCount = eficienciaEmpresaAdapter.getItemCount();

                    if (lastVisibleItemPosition >= totalItemCount - 1) {
                        recyclerViewEficienciaEmpresa.smoothScrollToPosition(0);
                    } else {
                        recyclerViewEficienciaEmpresa.smoothScrollToPosition(lastVisibleItemPosition + 1);
                    }
                    sendEmptyMessageDelayed(SCROLL_MESSAGE, 5000);
                }
            }
        }
    };

    public UsoEficienteEmpresaFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_uso_eficiente_empresa, container, false);

        // Configurar RecyclerView para Uso Empresa
        recyclerViewUsoEmpresa = root.findViewById(R.id.recycler_view_uso_empresa);
        horizontalLayoutManagerUso = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewUsoEmpresa.setLayoutManager(horizontalLayoutManagerUso);
        configureUsoEmpresaAdapter();

        // Configurar RecyclerView para Eficiencia Empresa
        recyclerViewEficienciaEmpresa = root.findViewById(R.id.recyclerEficienciaEmpresa);
        horizontalLayoutManagerEficiencia = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewEficienciaEmpresa.setLayoutManager(horizontalLayoutManagerEficiencia);
        configureEficienciaEmpresaAdapter();


        // Configurar FloatingActionButton
        FloatingActionButton fabAdd = root.findViewById(R.id.addUsoEmpresa);
        configureFloatingActionButton(fabAdd);

        return root;
    }

    private void configureUsoEmpresaAdapter() {
        Query usoEmpresaQuery = mFirestore.collection("uso_empresas")
                .orderBy("fecha", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<UsoEmpresa> options =
                new FirestoreRecyclerOptions.Builder<UsoEmpresa>()
                        .setQuery(usoEmpresaQuery, UsoEmpresa.class)
                        .build();

        usoEmpresaAdapter = new UsoEmpresaAdapter(options, getContext());
        recyclerViewUsoEmpresa.setAdapter(usoEmpresaAdapter);
    }

    private void configureEficienciaEmpresaAdapter() {
        Query eficienciaEmpresaQuery = mFirestore.collection("eficiencia_empresa")
                .orderBy("fecha", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<EficienciaEmpresa> options =
                new FirestoreRecyclerOptions.Builder<EficienciaEmpresa>()
                        .setQuery(eficienciaEmpresaQuery, EficienciaEmpresa.class)
                        .build();

        eficienciaEmpresaAdapter = new EficienciaEmpresaAdapter(options, getContext());
        recyclerViewEficienciaEmpresa.setAdapter(eficienciaEmpresaAdapter);
    }

    private void configureFloatingActionButton(FloatingActionButton fabAdd) {
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            fabAdd.setVisibility(View.VISIBLE);
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        fabAdd.setOnClickListener(v -> mostrarDialogoSeleccion());
    }

    private void mostrarDialogoSeleccion() {
        new AlertDialog.Builder(getContext())
                .setTitle("Seleccionar una opción")
                .setItems(new CharSequence[]{"Agregar Uso Empresa", "Agregar Eficiencia Empresa"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            startCreateUsoEmpresaActivity();
                            break;
                        case 1:
                            startCreateUsoIndustriaActivity();
                            break;
                    }
                })
                .create()
                .show();
    }

    private void startCreateUsoEmpresaActivity() {
        Intent intent = new Intent(getContext(), UsoEmpresactivity.class);
        startActivityForResult(intent, CREATE_USO_EMPRESA_REQUEST_CODE);
    }

    private void startCreateUsoIndustriaActivity() {
        Intent intent = new Intent(getContext(), CreateEficienciaEmpresaActivity.class);
        startActivityForResult(intent, CREATE_USO_EMPRESA_REQUEST_CODE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (usoEmpresaAdapter != null) {
            usoEmpresaAdapter.startListening();
        }
        if (eficienciaEmpresaAdapter != null) {
            eficienciaEmpresaAdapter.startListening();
        }
        scrollHandler.sendEmptyMessageDelayed(SCROLL_MESSAGE, 5000);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (usoEmpresaAdapter != null) {
          //  usoEmpresaAdapter.stopListening();
        }
        if (eficienciaEmpresaAdapter != null) {
          //  eficienciaEmpresaAdapter.stopListening();
        }
        scrollHandler.removeMessages(SCROLL_MESSAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // Manejar resultados si es necesario
        }
    }
}
