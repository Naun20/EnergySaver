package com.ejercicio2.energysaver.ui.slideshow;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.CreateConsejoFragment;
import com.ejercicio2.energysaver.MostrarConsejoFragment;
import com.ejercicio2.energysaver.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ConsejoAdapter extends RecyclerView.Adapter<ConsejoAdapter.ViewHolder> {

    private static final int REQUEST_CODE_EDIT = 1001;
    private List<Consejo> consejos;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public ConsejoAdapter(Context context, List<Consejo> consejos) {
        this.context = context;
        this.consejos = consejos;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_consejos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Consejo consejo = consejos.get(position);

        holder.titleTextView.setText(consejo.getTitulo());
        holder.fechaTextView.setText(consejo.getFecha());

        Glide.with(context)
                .load(consejo.getImagenUrl())
                .into(holder.imageView);


        // Verificar si el usuario está autenticado por correo
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
        }




        // Lógica para eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            String documentId = consejo.getTitulo(); // Usar el título como ID del documento
            String imageUrl = consejo.getImagenUrl(); // Obtener la URL de la imagen

            new AlertDialog.Builder(context)
                    .setTitle("Eliminar consejo")
                    .setMessage("¿Estás seguro de que deseas eliminar este consejo?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Primero eliminar la imagen
                            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                            imageRef.delete().addOnSuccessListener(aVoid1 -> {
                                // Después eliminar el documento
                                db.collection("consejos").document(documentId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Eliminar el consejo de la lista
                                            consejos.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, consejos.size());
                                            Toast.makeText(context, "Consejo e imagen eliminados exitosamente.", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Error al eliminar el consejo.", Toast.LENGTH_SHORT).show();
                                        });
                            }).addOnFailureListener(e -> {
                                Toast.makeText(context, "Error al eliminar la imagen.", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // Si no hay URL de imagen, solo eliminar el documento
                            db.collection("consejos").document(documentId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Eliminar el consejo de la lista
                                        consejos.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, consejos.size());
                                        Toast.makeText(context, "Consejo eliminado exitosamente.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error al eliminar el consejo.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .create()
                    .show();
        });


        // Lógica para editar
        holder.btnEditar.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("titulo", consejo.getTitulo());
            bundle.putString("descripcion", consejo.getDescripcion());
            bundle.putString("fecha", consejo.getFecha());
            bundle.putString("imageUrl", consejo.getImagenUrl());

            CreateConsejoFragment createConsejoFragment = CreateConsejoFragment.newInstance(
                    consejo.getTitulo(),
                    consejo.getDescripcion(),
                    consejo.getFecha(),
                    consejo.getImagenUrl()
            );

            createConsejoFragment.setArguments(bundle);

            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                createConsejoFragment.show(activity.getSupportFragmentManager(), "createConsejoFragment");
            }
        });

        // Lógica para mostrar el consejo
        holder.itemView.setOnClickListener(v -> {
            MostrarConsejoFragment mostrarConsejoFragment = MostrarConsejoFragment.newInstance(
                    consejo.getImagenUrl(),
                    consejo.getTitulo(),
                    consejo.getDescripcion(),
                    consejo.getFecha()
            );

            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                mostrarConsejoFragment.show(activity.getSupportFragmentManager(), "mostrarConsejoFragment");
            }
        });
    }

    @Override
    public int getItemCount() {
        return consejos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView, fechaTextView;
        ImageView btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewconsejos);
            titleTextView = itemView.findViewById(R.id.titleTextViewconsejos);
            fechaTextView = itemView.findViewById(R.id.titleTextViewfecha);
            btnEditar = itemView.findViewById(R.id.btn_editar_consejo);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_consejo);
        }
    }
}
