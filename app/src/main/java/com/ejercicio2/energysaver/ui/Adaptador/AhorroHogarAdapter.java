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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.CreateAhorroHogarActivity;
import com.ejercicio2.energysaver.MostrarAhorroFragment;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.ui.Clase.AhorroHogar;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AhorroHogarAdapter extends FirestoreRecyclerAdapter<AhorroHogar, AhorroHogarAdapter.AhorroHogarViewHolder> {

    private Context context;
    private FirebaseFirestore db;
    private boolean isAdmin;
    private FirebaseAuth auth;

    public AhorroHogarAdapter(@NonNull FirestoreRecyclerOptions<AhorroHogar> options, Context context) {
        super(options);
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();


    }

    @Override
    protected void onBindViewHolder(@NonNull AhorroHogarViewHolder holder, int position, @NonNull AhorroHogar model) {
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
            deleteConsejo(documentId, model.getImagenUrl());
        });

        holder.itemView.setOnClickListener(v -> {
            MostrarAhorroFragment mostrarAhorroFragment = MostrarAhorroFragment.newInstance(
                    model.getTitulo(), model.getDescripcion(), model.getFecha(), model.getImagenUrl());

            FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, mostrarAhorroFragment)
                    .addToBackStack(null)
                    .commit();
        });


        holder.btnEditar.setOnClickListener(view -> {
            Intent intent = new Intent(context, CreateAhorroHogarActivity.class);
            intent.putExtra("EXISTING_AHORRO_HOGAR", model);
            intent.putExtra("ID", model.getTitulo());
            intent.putExtra("DESCRIPCION", model.getDescripcion()); // Añadir la descripción
            context.startActivity(intent);
        });
    }

    private void deleteConsejo(String documentId, String imageUrl) {
        db.collection("ahorro_hogar").document(documentId).delete().addOnSuccessListener(aVoid -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                imageRef.delete().addOnSuccessListener(aVoid1 -> {
                    Toast.makeText(context, "Consejo eliminado correctamente.", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Consejo eliminado, pero hubo un error al eliminar la imagen.", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(context, "Consejo eliminado correctamente.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Error al eliminar el consejo.", Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    @Override
    public AhorroHogarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ahorro, parent, false);
        return new AhorroHogarViewHolder(view);
    }

    static class AhorroHogarViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView fechaTextView;
        ImageView btnEliminar;
        ImageView btnEditar;

        public AhorroHogarViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
            titleTextView = itemView.findViewById(R.id.Titulo);
            fechaTextView = itemView.findViewById(R.id.fechaahorro);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
            btnEditar = itemView.findViewById(R.id.btn_editar);
        }
    }
}
