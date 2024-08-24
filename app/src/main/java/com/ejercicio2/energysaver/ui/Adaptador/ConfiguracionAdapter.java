package com.ejercicio2.energysaver.ui.Adaptador;

import android.app.AlertDialog;
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
import com.ejercicio2.energysaver.ConfiguracionesActivity;
import com.ejercicio2.energysaver.MostrarUsoFragment;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.ui.Clase.Configuracion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ConfiguracionAdapter extends RecyclerView.Adapter<ConfiguracionAdapter.ConfiguracionViewHolder> {

    private Context context;
    private List<Configuracion> configuracionList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;


    public ConfiguracionAdapter(Context context, List<Configuracion> configuracionList) {
        this.context = context;
        this.configuracionList = configuracionList;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();

      }

    @NonNull
    @Override
    public ConfiguracionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_configuraciones, parent, false);
        return new ConfiguracionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfiguracionViewHolder holder, int position) {
        Configuracion configuracion = configuracionList.get(position);

        // Verificar si el usuario está autenticado por correo
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }

        // Vincula los datos con las vistas
        holder.titleTextView.setText(configuracion.getTitulo());
        holder.descriptionTextView.setText(configuracion.getDescripcion());
        holder.dateTextView.setText(configuracion.getFecha());

        Glide.with(context)
                .load(configuracion.getImagenUrl())
                .into(holder.imageView);


        holder.itemView.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            MostrarUsoFragment dialogFragment = MostrarUsoFragment.newInstance(
                    configuracion.getImagenUrl(),
                    configuracion.getTitulo(),
                    configuracion.getDescripcion() // Pasa la descripción aquí
            );
            dialogFragment.show(fragmentManager, "MostrarUsoFragment");
        });

        holder.deleteButton.setOnClickListener(v -> {
            String documentId = configuracion.getTitulo();
            String imageUrl = configuracion.getImagenUrl();

            new AlertDialog.Builder(context)
                    .setTitle("Eliminar configuración")
                    .setMessage("¿Estás seguro de que deseas eliminar esta configuración?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        db.collection("configuraciones").document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    if (imageUrl != null && !imageUrl.isEmpty()) {
                                        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                        imageRef.delete().addOnSuccessListener(aVoid1 -> {
                                            int index = findItemIndexByTitle(documentId);
                                            if (index != -1) {
                                                configuracionList.remove(index);
                                                notifyItemRemoved(index);
                                                notifyItemRangeChanged(index, configuracionList.size());
                                                Toast.makeText(context, "Configuración y imagen eliminados correctamente.", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(context, "Configuración eliminada, pero hubo un error al eliminar la imagen.", Toast.LENGTH_SHORT).show();
                                        });
                                    } else {
                                        int index = findItemIndexByTitle(documentId);
                                        if (index != -1) {
                                            configuracionList.remove(index);
                                            notifyItemRemoved(index);
                                            notifyItemRangeChanged(index, configuracionList.size());
                                            Toast.makeText(context, "Configuración eliminada correctamente.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error al eliminar la configuración.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancelar", null)
                    .create()
                    .show();
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ConfiguracionesActivity.class);
            intent.putExtra("EXISTING_CONFIGURACION", configuracion);
            intent.putExtra("ID", configuracion.getTitulo());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return configuracionList.size();
    }

    private int findItemIndexByTitle(String title) {
        for (int i = 0; i < configuracionList.size(); i++) {
            if (configuracionList.get(i).getTitulo().equals(title)) {
                return i;
            }
        }
        return -1;
    }

    static class ConfiguracionViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView dateTextView;
        ImageView deleteButton;
        ImageView editButton;

        public ConfiguracionViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            titleTextView = itemView.findViewById(R.id.item_title);
            descriptionTextView = itemView.findViewById(R.id.item_description);
            dateTextView = itemView.findViewById(R.id.item_date);
            deleteButton = itemView.findViewById(R.id.btn_eliminar_configuracion);
            editButton = itemView.findViewById(R.id.btn_editar_configuracion);
        }
    }
}
