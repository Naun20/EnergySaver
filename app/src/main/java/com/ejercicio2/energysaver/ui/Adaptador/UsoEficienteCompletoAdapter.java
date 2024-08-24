// UsoEficienteCompletoAdapter.java
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
import com.ejercicio2.energysaver.GuiaActivity;
import com.ejercicio2.energysaver.MostrarUsoFragment;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.ui.Clase.UsoEficiente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class UsoEficienteCompletoAdapter extends RecyclerView.Adapter<UsoEficienteCompletoAdapter.UsoEficienteViewHolder> {

    private Context context;
    private List<UsoEficiente> usoEficienteList;
    private FirebaseFirestore db;
    private boolean isAdmin;
    private FirebaseAuth auth;

    public UsoEficienteCompletoAdapter(Context context, List<UsoEficiente> usoEficienteList) {
        this.context = context;
        this.usoEficienteList = usoEficienteList;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();

    }

    @NonNull
    @Override
    public UsoEficienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guias, parent, false);
        return new UsoEficienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsoEficienteViewHolder holder, int position) {
        UsoEficiente usoEficiente = usoEficienteList.get(position);

        holder.titleTextView.setText(usoEficiente.getTitulo());

        Glide.with(context)
                .load(usoEficiente.getImagenUrl())
                .into(holder.imageView);

        // Verificar si el usuario está autenticado por correo
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            MostrarUsoFragment dialogFragment = MostrarUsoFragment.newInstance(
                    usoEficiente.getImagenUrl(),
                    usoEficiente.getTitulo(),
                    usoEficiente.getDescripcion()
            );
            dialogFragment.show(fragmentManager, "MostrarUsoFragment");
        });

        holder.deleteButton.setOnClickListener(v -> {
            String documentId = usoEficiente.getTitulo();
            String imageUrl = usoEficiente.getImagenUrl();

            new AlertDialog.Builder(context)
                    .setTitle("Eliminar uso eficiente")
                    .setMessage("¿Estás seguro de que deseas eliminar este uso eficiente?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        db.collection("uso_eficiente").document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    if (imageUrl != null && !imageUrl.isEmpty()) {
                                        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                        imageRef.delete().addOnSuccessListener(aVoid1 -> {
                                            int index = findItemIndexByTitle(documentId);
                                            if (index != -1) {
                                                usoEficienteList.remove(index);
                                                notifyItemRemoved(index);
                                                notifyItemRangeChanged(index, usoEficienteList.size());
                                                Toast.makeText(context, "Uso eficiente y imagen eliminados correctamente.", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(context, "Uso eficiente eliminado, pero hubo un error al eliminar la imagen.", Toast.LENGTH_SHORT).show();
                                        });
                                    } else {
                                        int index = findItemIndexByTitle(documentId);
                                        if (index != -1) {
                                            usoEficienteList.remove(index);
                                            notifyItemRemoved(index);
                                            notifyItemRangeChanged(index, usoEficienteList.size());
                                            Toast.makeText(context, "Uso eficiente eliminado correctamente.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error al eliminar el uso eficiente.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancelar", null)
                    .create()
                    .show();
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, GuiaActivity.class);
            intent.putExtra("EXISTING_USO_EFICIENTE", usoEficiente);
            intent.putExtra("ID", usoEficiente.getTitulo());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usoEficienteList.size();
    }

    private int findItemIndexByTitle(String title) {
        for (int i = 0; i < usoEficienteList.size(); i++) {
            if (usoEficienteList.get(i).getTitulo().equals(title)) {
                return i;
            }
        }
        return -1;
    }

    public static class UsoEficienteViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        ImageView deleteButton;
        ImageView editButton;

        public UsoEficienteViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            titleTextView = itemView.findViewById(R.id.item_text);
            deleteButton = itemView.findViewById(R.id.btn_eliminar_consejo);
            editButton = itemView.findViewById(R.id.btn_editar_consejo);
        }
    }
}
