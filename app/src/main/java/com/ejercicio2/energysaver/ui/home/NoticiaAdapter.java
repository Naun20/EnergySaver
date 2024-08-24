package com.ejercicio2.energysaver.ui.home;

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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.CreateNoticiaActivity;
import com.ejercicio2.energysaver.MostrarNoticiaFragment;
import com.ejercicio2.energysaver.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class NoticiaAdapter extends RecyclerView.Adapter<NoticiaAdapter.ViewHolder> {

    private List<Noticia> noticias;
    private Context context;
    private FirebaseFirestore db;
    private boolean isAdmin;
    private boolean isGoogleUser;
    private FirebaseAuth auth;



    public NoticiaAdapter(Context context, List<Noticia> noticias) {
        this.context = context;
        this.noticias = noticias;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();

        // Leer el estado de admin del usuario desde SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        isAdmin = sharedPreferences.getBoolean("isAdmin", false); // Por defecto, no es admin
        isGoogleUser = sharedPreferences.getBoolean("isGoogleUser", false); // Por defecto, no es usuario de Google
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.noticia_diseno, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Noticia noticia = noticias.get(position);

        holder.titulo.setText(noticia.getTitulo());
        holder.fecha.setText(noticia.getFecha());

        Glide.with(context)
                .load(noticia.getImagenUrl())
                .into(holder.imagenPrincipal);

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
            String documentId = noticia.getTitulo(); // Usar el título como ID del documento
            String imageUrl = noticia.getImagenUrl(); // Obtener la URL de la imagen

            new AlertDialog.Builder(context)
                    .setTitle("Eliminar noticia")
                    .setMessage("¿Estás seguro de que deseas eliminar esta noticia?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        db.collection("noticias").document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                    imageRef.delete().addOnSuccessListener(aVoid1 -> {
                                        noticias.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, noticias.size());
                                        Toast.makeText(context, "Noticia y imagen eliminadas exitosamente.", Toast.LENGTH_SHORT).show();
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error al eliminar la imagen.", Toast.LENGTH_SHORT).show();
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error al eliminar la noticia.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancelar", null)
                    .create()
                    .show();
        });

        // Lógica para mostrar la noticia
        holder.itemView.setOnClickListener(v -> {
            MostrarNoticiaFragment mostrarNoticiaFragment = MostrarNoticiaFragment.newInstance(
                    noticia.getTitulo(),
                    noticia.getDescripcion(),
                    noticia.getFecha(),
                    noticia.getImagenUrl(),
                    noticia.getEnlace()
            );

            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                mostrarNoticiaFragment.show(activity.getSupportFragmentManager(), "MostrarNoticiaFragment");
            }
        });

        // Lógica para editar
        holder.btnEditar.setOnClickListener(v -> {
            // Crear un Intent para iniciar CreateNovedadActivity
            Intent intent = new Intent(context, CreateNoticiaActivity.class);
            intent.putExtra("documentId", noticia.getTitulo()); // ID del documento
            intent.putExtra("titulo", noticia.getTitulo());
            intent.putExtra("descripcion", noticia.getDescripcion());
            intent.putExtra("enlace", noticia.getEnlace());
            intent.putExtra("imageUrl", noticia.getImagenUrl());
            intent.putExtra("isEditing", true); // Indicar que es un modo de edición
            context.startActivity(intent);

        });


    }

    @Override
    public int getItemCount() {
        return noticias.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenPrincipal;
        TextView titulo, fecha;
        ImageView btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenPrincipal = itemView.findViewById(R.id.imagen_principal);
            titulo = itemView.findViewById(R.id.titulo);
            fecha = itemView.findViewById(R.id.fecha);
            btnEditar = itemView.findViewById(R.id.btn_editar_noticia2);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_noticia2);
        }
    }
}
