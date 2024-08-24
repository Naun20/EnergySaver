package com.ejercicio2.energysaver.ui.Adaptador;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.CreateEficienciaActivity;
import com.ejercicio2.energysaver.MostrarUsoFragment;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.ui.Clase.Eficiencia;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EficienciaAdapter extends FirestoreRecyclerAdapter<Eficiencia, EficienciaAdapter.EficienciaViewHolder> {

    private Context context;
    private FirebaseFirestore db;
    private boolean isAdmin;
    private FirebaseAuth auth;

    public EficienciaAdapter(@NonNull FirestoreRecyclerOptions<Eficiencia> options, Context context) {
        super(options);
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();


    }

    @Override
    protected void onBindViewHolder(@NonNull EficienciaViewHolder holder, int position, @NonNull Eficiencia model) {
        holder.titleTextView.setText(model.getTitulo());
        holder.descriptionTextView.setText(model.getDescripcion());

        Glide.with(context)
                .load(model.getImagenUrl())
                .into(holder.imageView);

        // Verificar si el usuario está autenticado por correo
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            holder.btnEditarConsejo.setVisibility(View.VISIBLE);
            holder.btnEliminarConsejo.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditarConsejo.setVisibility(View.GONE);
            holder.btnEliminarConsejo.setVisibility(View.GONE);
        }

        // Forzar actualización de la visibilidad para evitar reciclado incorrecto
        holder.btnEliminarConsejo.invalidate();
        holder.btnEditarConsejo.invalidate();

        holder.itemView.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            MostrarUsoFragment dialogFragment = MostrarUsoFragment.newInstance(
                    model.getImagenUrl(),
                    model.getTitulo(),
                    model.getDescripcion()
            );
            dialogFragment.show(fragmentManager, "MostrarUsoFragment");
        });

        holder.btnEliminarConsejo.setOnClickListener(view -> {
            String documentId = model.getTitulo();
            deleteConsejo(documentId, model.getImagenUrl(), position);
        });

        holder.btnEditarConsejo.setOnClickListener(view -> {
            Intent intent = new Intent(context, CreateEficienciaActivity.class);
            intent.putExtra("EXISTING_EFICIENCIA", model);
            intent.putExtra("ID", model.getTitulo());
            context.startActivity(intent);
        });
    }


    private void deleteConsejo(String documentId, String imageUrl, int position) {
        db.collection("eficiencia_energetica").document(documentId).delete().addOnSuccessListener(aVoid -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                imageRef.delete().addOnSuccessListener(aVoid1 -> {
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Consejo eliminado correctamente.", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Consejo eliminado, pero hubo un error al eliminar la imagen.", Toast.LENGTH_SHORT).show();
                });
            } else {
                notifyItemRemoved(position);
                Toast.makeText(context, "Consejo eliminado correctamente.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Error al eliminar el consejo.", Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    @Override
    public EficienciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eficiencia_energetica, parent, false);
        return new EficienciaViewHolder(view);
    }

    static class EficienciaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView descriptionTextView;
        ImageView btnEliminarConsejo;
        ImageView btnEditarConsejo;  // Botón de edición

        public EficienciaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
            titleTextView = itemView.findViewById(R.id.textViewTitulo);
            descriptionTextView = itemView.findViewById(R.id.textViewDescripcion);
            btnEliminarConsejo = itemView.findViewById(R.id.btn_eliminar_consejo);
            btnEditarConsejo = itemView.findViewById(R.id.btn_editar_consejo);  // Inicializar el botón de edición
        }
    }
}
