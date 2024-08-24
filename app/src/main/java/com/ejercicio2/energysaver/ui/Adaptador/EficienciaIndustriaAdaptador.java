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
import com.ejercicio2.energysaver.MostrarEficienciaIndustriaFragment;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.ui.Clase.EficienciaIndustrias;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EficienciaIndustriaAdaptador extends FirestoreRecyclerAdapter<EficienciaIndustrias, EficienciaIndustriaAdaptador.EficienciaIndustriaViewHolder> {

    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public EficienciaIndustriaAdaptador(@NonNull FirestoreRecyclerOptions<EficienciaIndustrias> options, Context context) {
        super(options);
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onBindViewHolder(@NonNull EficienciaIndustriaViewHolder holder, int position, @NonNull EficienciaIndustrias model) {
        holder.titleTextView.setText(model.getTitulo());
        holder.fechaTextView.setText(model.getFecha());

        Glide.with(context)
                .load(model.getImagenUrl())
                .into(holder.imageView);

        // Verificar si el usuario está autenticado por correo
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
        }

        holder.btnEliminar.setOnClickListener(view -> {
            String documentId = model.getTitulo();
            deleteEficiencia(documentId, model.getImagenUrl());
        });

        // Mostrar el fragmento al hacer clic en el elemento
        holder.itemView.setOnClickListener(v -> {
            MostrarEficienciaIndustriaFragment mostrarEficienciaFragment = MostrarEficienciaIndustriaFragment.newInstance(
                    model.getImagenUrl(), model.getTitulo(), model.getDescripcion(), model.getFecha());

            FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, mostrarEficienciaFragment)
                    .addToBackStack(null)
                    .commit();
        });

        holder.btnEditar.setOnClickListener(view -> {
            Intent intent = new Intent(context, CreateEficienciaIndustriaActivity.class);
            intent.putExtra("EXISTING_EFICIENCIA_INDUSTRIA", model); // Aquí se pasa el objeto serializable completo
            context.startActivity(intent);
        });

    }

    private void deleteEficiencia(String documentId, String imageUrl) {
        db.collection("eficiencia_industrias").document(documentId).delete().addOnSuccessListener(aVoid -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                imageRef.delete().addOnSuccessListener(aVoid1 -> {
                    Toast.makeText(context, "Eficiencia eliminada correctamente.", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Eficiencia eliminada, pero hubo un error al eliminar la imagen.", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(context, "Eficiencia eliminada correctamente.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Error al eliminar la eficiencia.", Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    @Override
    public EficienciaIndustriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eficiencia_industria, parent, false);
        return new EficienciaIndustriaViewHolder(view);
    }

    static class EficienciaIndustriaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView fechaTextView;
        ImageView btnEliminar;
        ImageView btnEditar;

        public EficienciaIndustriaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
            titleTextView = itemView.findViewById(R.id.Titulo);
            fechaTextView = itemView.findViewById(R.id.fechaeficiencia);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
            btnEditar = itemView.findViewById(R.id.btn_editar);
        }
    }
}
