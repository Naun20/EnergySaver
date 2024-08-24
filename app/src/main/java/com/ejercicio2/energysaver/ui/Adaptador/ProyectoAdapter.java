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
import com.ejercicio2.energysaver.CreateEficienciaIndustriaActivity;
import com.ejercicio2.energysaver.CreateProyectoActivity;
import com.ejercicio2.energysaver.MostrarEficienciaIndustriaFragment;
import com.ejercicio2.energysaver.MostrarProyectoFragment;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.ui.Clase.EficienciaIndustrias;
import com.ejercicio2.energysaver.ui.Clase.Proyecto;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProyectoAdapter extends FirestoreRecyclerAdapter<Proyecto, ProyectoAdapter.ProyectoViewHolder> {

    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public ProyectoAdapter(@NonNull FirestoreRecyclerOptions<Proyecto> options, Context context) {
        super(options);
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onBindViewHolder(@NonNull ProyectoViewHolder holder, int position, @NonNull Proyecto model) {
        holder.titleTextView.setText(model.getTitulo());
        holder.fechaTextView.setText(model.getFecha());

        Glide.with(context)
                .load(model.getImagenUrl())
                .into(holder.imageView);

        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
        }

        // Configurar el click listener para mostrar el fragmento de detalle del proyecto
        holder.itemView.setOnClickListener(v -> {
            MostrarProyectoFragment mostrarProyectoFragment = MostrarProyectoFragment.newInstance(
                    model.getImagenUrl(), model.getTitulo(), model.getDescripcion(), model.getFecha());

            FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, mostrarProyectoFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Listener para eliminar el proyecto
        holder.btnEliminar.setOnClickListener(view -> {
            String documentId = model.getTitulo();  // Asumiendo que el título es el ID del documento
            deleteProyecto(documentId, model.getImagenUrl());
        });

        // Listener para editar el proyecto
        holder.btnEditar.setOnClickListener(view -> {
            Intent intent = new Intent(context, CreateProyectoActivity.class);
            intent.putExtra("EXISTING_PROYECTO", model);
            context.startActivity(intent);
        });
    }

    private void deleteProyecto(String documentId, String imageUrl) {
        db.collection("proyectos").document(documentId).delete().addOnSuccessListener(aVoid -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                imageRef.delete().addOnSuccessListener(aVoid1 -> {
                    Toast.makeText(context, "Proyecto eliminado correctamente.", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Proyecto eliminado, pero hubo un error al eliminar la imagen.", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(context, "Proyecto eliminado correctamente.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Error al eliminar el proyecto.", Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    @Override
    public ProyectoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_proyecto, parent, false);
        return new ProyectoViewHolder(view);
    }

    static class ProyectoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView fechaTextView;
        ImageView btnEliminar;
        ImageView btnEditar;

        public ProyectoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            titleTextView = itemView.findViewById(R.id.item_text);
            fechaTextView = itemView.findViewById(R.id.item_fecha);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_consejo);
            btnEditar = itemView.findViewById(R.id.btn_editar_consejo);
        }
    }
}
