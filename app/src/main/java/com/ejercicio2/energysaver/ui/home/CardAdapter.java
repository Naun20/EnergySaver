package com.ejercicio2.energysaver.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.CreateNovedadActivity;
import com.ejercicio2.energysaver.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private List<Novedad> novedades;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public CardAdapter(Context context, List<Novedad> novedades) {
        this.context = context;
        this.novedades = novedades;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Novedad novedad = novedades.get(position);

        holder.title.setText(novedad.getTitulo());
        holder.description.setText(novedad.getDescripcion());

        Glide.with(context)
                .load(novedad.getImageUrl())
                .into(holder.imageNovedad);

        holder.buttonMore.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(novedad.getEnlace()));
            context.startActivity(browserIntent);
        });

        // Verificar si el usuario está autenticado por correo
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            holder.buttonEdit.setVisibility(View.VISIBLE);
            holder.buttonDelete.setVisibility(View.VISIBLE);
        } else {
            holder.buttonEdit.setVisibility(View.GONE);
            holder.buttonDelete.setVisibility(View.GONE);
        }

        // Lógica para eliminar
        holder.buttonDelete.setOnClickListener(v -> {
            String documentId = novedad.getTitulo(); // Usar el título como ID del documento

            db.collection("novedades").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        novedades.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, novedades.size());
                    })
                    .addOnFailureListener(e -> {
                        // Manejar el error
                    });
        });

        // Lógica para editar

        holder.buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, CreateNovedadActivity.class);
            intent.putExtra("titulo", novedad.getTitulo());
            intent.putExtra("descripcion", novedad.getDescripcion());
            intent.putExtra("enlace", novedad.getEnlace());
            intent.putExtra("imageUrl", novedad.getImageUrl());
            intent.putExtra("isEditing", true); // Indicar que es un modo de edición
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return novedades.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageNovedad;
        TextView title;
        TextView description;
        Button buttonMore;
        ImageButton buttonEdit;
        ImageButton buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageNovedad = itemView.findViewById(R.id.imagenovedad);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.descriptionnovedad);
            buttonMore = itemView.findViewById(R.id.button_more);
            buttonEdit = itemView.findViewById(R.id.button_editarnovedad);
            buttonDelete = itemView.findViewById(R.id.button_eliminarnovedad);
        }
    }
}
