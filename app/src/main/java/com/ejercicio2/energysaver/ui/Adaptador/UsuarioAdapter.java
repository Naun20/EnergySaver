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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.CreateUsuarioActivity;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.ui.Clase.Usuario;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UsuarioAdapter extends FirestoreRecyclerAdapter<Usuario, UsuarioAdapter.UsuarioViewHolder> {

    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public UsuarioAdapter(@NonNull FirestoreRecyclerOptions<Usuario> options, Context context) {
        super(options);
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position, @NonNull Usuario model) {
        holder.nombreCompletoTextView.setText(model.getNombreCompleto());
        holder.tipoUsuarioTextView.setText(model.getTipoUsuario());
        holder.nombreUsuarioTextView.setText(model.getNombreUsuario());
        holder.emailUserTextView.setText(model.getEmail());

        Glide.with(context)
                .load(model.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.photoImageView);

        holder.btnEliminarUsuario.setOnClickListener(view -> {
            String documentId = getSnapshots().getSnapshot(position).getId(); // Obtener el ID del documento
            deleteUserAndData(documentId, position);
        });


        holder.btnEditarUsuario.setOnClickListener(view -> {
            Intent intent = new Intent(context, CreateUsuarioActivity.class);
            intent.putExtra("EXISTING_USUARIO", model);
            context.startActivity(intent);
        });
    }

    private void deleteUserAndData(String documentId, int position) {
        DocumentReference userDocRef = db.collection("usuarios").document(documentId);

        // Obtener el documento del usuario
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String imageUrl = documentSnapshot.getString("imageUrl");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Si hay una imagen asociada, eliminarla de Firebase Storage
                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    imageRef.delete().addOnSuccessListener(aVoid -> {
                        // Después de eliminar la imagen, eliminar el documento de Firestore y el usuario autenticado
                        deleteUserDocumentAndAuth(userDocRef, position);
                    }).addOnFailureListener(e -> {
                        // Si falla la eliminación de la imagen, mostrar un mensaje e intentar eliminar el documento y el usuario de todas formas
                        Toast.makeText(context, "Error al eliminar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        deleteUserDocumentAndAuth(userDocRef, position);
                    });
                } else {
                    // Si no hay imagen, proceder directamente a eliminar el documento de Firestore y el usuario autenticado
                    deleteUserDocumentAndAuth(userDocRef, position);
                }
            } else {
                // Si el documento no existe, no hacer nada
                Toast.makeText(context, "El documento del usuario no existe.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Error al obtener el documento
            Toast.makeText(context, "Error al obtener el documento del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteUserDocumentAndAuth(DocumentReference userDocRef, int position) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                // Primero, eliminar el documento de Firestore
                userDocRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            // Después de eliminar el documento, eliminar el usuario autenticado
                            currentUser.delete()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            notifyItemRemoved(position);
                                            Toast.makeText(context, "Usuario y documento eliminados correctamente.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "Error al eliminar el usuario de Firebase Authentication: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error al eliminar el usuario de Firebase Authentication: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            // Error al eliminar el documento de Firestore
                            Toast.makeText(context, "Error al eliminar el documento del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // No se encontró el email en el usuario autenticado
                Toast.makeText(context, "No se encontró el email del usuario autenticado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;
        TextView nombreCompletoTextView;
        TextView tipoUsuarioTextView;
        TextView nombreUsuarioTextView;
        TextView emailUserTextView;
        ImageView btnEliminarUsuario;
        ImageView btnEditarUsuario;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photouser);
            nombreCompletoTextView = itemView.findViewById(R.id.nombrecompleto);
            tipoUsuarioTextView = itemView.findViewById(R.id.usertipo);
            nombreUsuarioTextView = itemView.findViewById(R.id.nombreusuario);
            emailUserTextView = itemView.findViewById(R.id.emailuser);
            btnEliminarUsuario = itemView.findViewById(R.id.btn_eliminar_usuario);
            btnEditarUsuario = itemView.findViewById(R.id.btn_editar_Usuario);
        }
    }
}
