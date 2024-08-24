package com.ejercicio2.energysaver.ui.Adaptador;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.MostrarUsoEmpresaFragment;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.UsoEmpresactivity;
import com.ejercicio2.energysaver.ui.Clase.UsoEmpresa;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UsoEmpresaAdapter extends FirestoreRecyclerAdapter<UsoEmpresa, UsoEmpresaAdapter.UsoEmpresaViewHolder> {

    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public UsoEmpresaAdapter(@NonNull FirestoreRecyclerOptions<UsoEmpresa> options, Context context) {
        super(options);
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onBindViewHolder(@NonNull UsoEmpresaViewHolder holder, int position, @NonNull UsoEmpresa model) {
        holder.titleTextView.setText(model.getTitulo());
        holder.fechaTextView.setText(model.getFecha());

        Glide.with(context)
                .load(model.getImagenUrl())
                .into(holder.imageView);

        // Mostrar u ocultar botones según el estado de autenticación del usuario
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
        }


        // Listener para mostrar el fragmento de detalle
        holder.itemView.setOnClickListener(v -> {
            MostrarUsoEmpresaFragment mostrarUsoEmpresaFragment = MostrarUsoEmpresaFragment.newInstance(
                    model.getImagenUrl(), model.getTitulo(), model.getDescripcion(), model.getFecha());

            FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, mostrarUsoEmpresaFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Listener para eliminar el uso de empresa
        holder.btnEliminar.setOnClickListener(view -> {
            String documentId = model.getTitulo();  // Asumiendo que el título es el ID del documento
            deleteUsoEmpresa(documentId, model.getImagenUrl());
        });

        // Listener para editar el uso de empresa
        holder.btnEditar.setOnClickListener(view -> {
            Intent intent = new Intent(context, UsoEmpresactivity.class);
            intent.putExtra("EXISTING_USO_EMPRESA", model);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public UsoEmpresaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_uso_empresa, parent, false);
        return new UsoEmpresaViewHolder(view);
    }

    public static class UsoEmpresaViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, fechaTextView;
        ImageView imageView, btnEditar, btnEliminar;

        public UsoEmpresaViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.item_text);
            fechaTextView = itemView.findViewById(R.id.item_fecha);
            imageView = itemView.findViewById(R.id.item_image);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
        }
    }

    private void deleteUsoEmpresa(String documentId, String imageUrl) {
        // Eliminar el documento de Firestore
        db.collection("uso_empresas").document(documentId).delete()
                .addOnSuccessListener(aVoid -> {
                    // Eliminar la imagen asociada en Firebase Storage
                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    imageRef.delete()
                            .addOnSuccessListener(aVoid1 -> Toast.makeText(context, "Uso Empresa eliminado", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar el uso de empresa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
