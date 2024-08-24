package com.ejercicio2.energysaver.ui.Adaptador;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.CreateUsoIndustriasActivity;
import com.ejercicio2.energysaver.MostrarUsoIndustriaFragment;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.ui.Clase.IndustriaEficiencia;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class IndustriaEficienciaAdapter extends FirestoreRecyclerAdapter<IndustriaEficiencia, IndustriaEficienciaAdapter.IndustriaEficienciaViewHolder> {

    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseStorage firebaseStorage;
    private final FirebaseAuth auth;

    public IndustriaEficienciaAdapter(@NonNull FirestoreRecyclerOptions<IndustriaEficiencia> options, Context context) {
        super(options);
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onBindViewHolder(@NonNull IndustriaEficienciaViewHolder holder, int position, @NonNull IndustriaEficiencia model) {
        holder.titleTextView.setText(model.getTitulo());

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

        // Configurar el evento click para mostrar los detalles en un DialogFragment
        holder.itemView.setOnClickListener(v -> {
            MostrarUsoIndustriaFragment mostrarIndustriaFragment = MostrarUsoIndustriaFragment.newInstance(
                    model.getTitulo(), model.getDescripcion(), model.getFecha(), model.getImagenUrl());

            // Mostrar el DialogFragment
            mostrarIndustriaFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "MostrarUsoIndustriaDialog");
        });

        holder.btnEditar.setOnClickListener(v -> {
            // Obtener el ID del documento
            String documentId = getSnapshots().getSnapshot(position).getId();

            // Intent para abrir la actividad de edición
            Intent intent = new Intent(context, CreateUsoIndustriasActivity.class);
            intent.putExtra("EXISTING_USO_INDUSTRIAS", model);
            intent.putExtra("DOCUMENT_ID", documentId); // Pasar el ID del documento para la edición

            // Verifica que los datos están siendo enviados
            Log.d("Editar", "Titulo: " + model.getTitulo() + ", Document ID: " + documentId);

            context.startActivity(intent);
        });



        // Configurar el botón de eliminación
        holder.btnEliminar.setOnClickListener(view -> {
            String documentId = getSnapshots().getSnapshot(position).getId();
            deleteData(documentId, model.getImagenUrl());
        });
    }

    @NonNull
    @Override
    public IndustriaEficienciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_uso_eficiente_industrias, parent, false);
        return new IndustriaEficienciaViewHolder(view);
    }

    // Método para eliminar datos y la imagen asociada
    private void deleteData(String documentId, String imageUrl) {
        // Eliminar el documento de Firestore
        db.collection("uso_industrias").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Eliminar la imagen de Firebase Storage si existe
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        StorageReference imageRef = firebaseStorage.getReferenceFromUrl(imageUrl);
                        imageRef.delete().addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(context, "Datos eliminados con éxito", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, "Error al eliminar la imagen", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(context, "Datos eliminados con éxito", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al eliminar los datos", Toast.LENGTH_SHORT).show();
                });
    }

    // ViewHolder para los elementos de la lista
    static class IndustriaEficienciaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        ImageView btnEliminar;
        ImageView btnEditar;

        public IndustriaEficienciaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageCover);
            titleTextView = itemView.findViewById(R.id.textTitle);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
            btnEditar = itemView.findViewById(R.id.btn_editar);
        }
    }
}
