package com.ejercicio2.energysaver.ui.Adaptador;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.ejercicio2.energysaver.CreateCalculoActivity;
import com.ejercicio2.energysaver.MostrarCalculoFragment;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.ui.Clase.Calculo;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CalculoAdapter extends FirestoreRecyclerAdapter<Calculo, CalculoAdapter.CalculoViewHolder> {

    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public CalculoAdapter(@NonNull FirestoreRecyclerOptions<Calculo> options, Context context) {
        super(options);
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onBindViewHolder(@NonNull CalculoViewHolder holder, int position, @NonNull Calculo model) {
        holder.titleTextView.setText(model.getTitulo());
        holder.fechaTextView.setText(model.getFecha());

        if (model.getImagenUrl() != null && !model.getImagenUrl().isEmpty()) {
            Log.d("CalculoAdapter", "Cargando imagen desde URL: " + model.getImagenUrl());
            Glide.with(context)
                    .load(model.getImagenUrl())
                    .into(holder.imageView);
        } else {
            Log.d("CalculoAdapter", "URL de imagen es nula o vacía");
            holder.imageView.setImageResource(R.drawable.logo);
        }

        // Mostrar u ocultar botones de edición y eliminación basado en el proveedor de autenticación
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
        }

        // Configurar el click listener para mostrar el fragmento de detalle del cálculo
        holder.itemView.setOnClickListener(v -> {
            MostrarCalculoFragment mostrarCalculoFragment = MostrarCalculoFragment.newInstance(
                    model.getTitulo(), model.getDescripcion(), model.getFecha(), model.getImagenUrl());

            FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, mostrarCalculoFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Listener para eliminar el cálculo
        holder.btnEliminar.setOnClickListener(view -> {
            String documentId = model.getTitulo();  // Asumiendo que el título es el ID del documento
            deleteCalculo(documentId, model.getImagenUrl());
        });

        // Listener para editar el cálculo
        holder.btnEditar.setOnClickListener(view -> {
            Intent intent = new Intent(context, CreateCalculoActivity.class);
            intent.putExtra("EXISTING_CALCULO", model);
            context.startActivity(intent);
        });
    }

    private void deleteCalculo(String documentId, String imageUrl) {
        db.collection("calculos").document(documentId).delete().addOnSuccessListener(aVoid -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                imageRef.delete().addOnSuccessListener(aVoid1 -> {
                    Toast.makeText(context, "Cálculo eliminado correctamente.", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Cálculo eliminado, pero hubo un error al eliminar la imagen.", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(context, "Cálculo eliminado correctamente.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Error al eliminar el cálculo.", Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    @Override
    public CalculoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calculo, parent, false);
        return new CalculoViewHolder(view);
    }

    static class CalculoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView fechaTextView;
        ImageView btnEliminar;
        ImageView btnEditar;

        public CalculoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            titleTextView = itemView.findViewById(R.id.item_text);
            fechaTextView = itemView.findViewById(R.id.item_fecha);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_calculo);
            btnEditar = itemView.findViewById(R.id.btn_editar_calculo);
        }
    }
}
