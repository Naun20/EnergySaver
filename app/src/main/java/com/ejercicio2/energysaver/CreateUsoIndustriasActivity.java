package com.ejercicio2.energysaver;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.ui.Clase.IndustriaEficiencia;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateUsoIndustriasActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView industriaImageView;
    private EditText tituloEditText, descripcionEditText;
    private TextView fechaTextView;
    private Button saveButton, deleteButton, addPhotoButton;
    private ProgressBar progressBar;

    private Uri imageUri;
    private String currentImageUrl;
    private String documentId; // ID del documento para la edición
    private boolean isEditing = false; // Indicador si estamos editando

    private FirebaseFirestore db;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_uso_industrias);

        // Configurar el padding para los bordes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        industriaImageView = findViewById(R.id.novedad_photo);
        tituloEditText = findViewById(R.id.txtTitulo);
        descripcionEditText = findViewById(R.id.txtDescripcion);
        fechaTextView = findViewById(R.id.txtFecha);
        saveButton = findViewById(R.id.btnGuardar);
        deleteButton = findViewById(R.id.btnEliminarFoto);
        addPhotoButton = findViewById(R.id.btn_agregarphoto_eficiencia);
        progressBar = findViewById(R.id.progressBar);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("uso_industrias");

        // Establecer fecha actual en el TextView de fecha
        fechaTextView.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        // Configurar el botón para seleccionar imagen
        addPhotoButton.setOnClickListener(v -> openFileChooser());

        // Configurar el botón para eliminar imagen
        deleteButton.setOnClickListener(v -> {
            industriaImageView.setImageResource(R.drawable.electrodomestico);
            imageUri = null;
        });

        // Configurar el botón para guardar
        saveButton.setOnClickListener(v -> saveUsoEficiente());


        // Manejar edición si se está editando un Uso Eficiente existente
        Intent intent = getIntent();
        if (intent.hasExtra("EXISTING_USO_INDUSTRIAS")) {
            isEditing = true;
            IndustriaEficiencia usoEficiencia = (IndustriaEficiencia) intent.getSerializableExtra("EXISTING_USO_INDUSTRIAS");
            documentId = intent.getStringExtra("DOCUMENT_ID"); // Obtener el ID del documento
            if (usoEficiencia != null) {
                tituloEditText.setText(usoEficiencia.getTitulo());
                descripcionEditText.setText(usoEficiencia.getDescripcion());
                currentImageUrl = usoEficiencia.getImagenUrl();
                if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                    Glide.with(this).load(currentImageUrl).into(industriaImageView);
                }
            }
            saveButton.setText("Actualizar");
        }
    }

    private void openFileChooser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, PICK_IMAGE_REQUEST);
            } else {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            } else {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
            }
        }    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                industriaImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUsoEficiente() {
        String titulo = tituloEditText.getText().toString().trim();
        String descripcion = descripcionEditText.getText().toString().trim();
        String fecha = fechaTextView.getText().toString().trim();

        if (TextUtils.isEmpty(titulo)) {
            tituloEditText.setError("Campo obligatorio");
            return;
        }
        if (TextUtils.isEmpty(descripcion)) {
            descripcionEditText.setError("Campo obligatorio");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            // Subir la nueva imagen
            StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String newImageUrl = uri.toString();
                        if (isEditing && currentImageUrl != null) {
                            // Primero eliminar la imagen antigua y luego guardar los nuevos datos
                            deleteOldImageAndSaveData(titulo, descripcion, fecha, newImageUrl);
                        } else {
                            // Si no es edición, simplemente guardar los nuevos datos
                            saveData(titulo, descripcion, fecha, newImageUrl);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateUsoIndustriasActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Si no hay imagen seleccionada, usar la URL de la imagen existente
            if (isEditing) {
                // Si estamos editando, y no hay nueva imagen, actualizar sin cambiar la imagen
                saveData(titulo, descripcion, fecha, currentImageUrl);
            } else {
                // Si no es edición y no hay nueva imagen, guardar el nuevo documento sin imagen
                saveData(titulo, descripcion, fecha, null);
            }
        }
    }

    private void deleteOldImageAndSaveData(String titulo, String descripcion, String fecha, String newImageUrl) {
        StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentImageUrl);
        oldImageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Después de eliminar la imagen antigua, guardar los nuevos datos
                    saveData(titulo, descripcion, fecha, newImageUrl);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateUsoIndustriasActivity.this, "Error al eliminar la imagen antigua: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveData(String titulo, String descripcion, String fecha, @Nullable String imageUrl) {
        Map<String, Object> usoEficienteMap = new HashMap<>();
        usoEficienteMap.put("titulo", titulo);
        usoEficienteMap.put("descripcion", descripcion);
        usoEficienteMap.put("fecha", fecha);
        usoEficienteMap.put("imagenUrl", imageUrl);

        if (isEditing) {
            // Actualizar el documento con el nuevo ID
            String newDocumentId = titulo; // Usar el nuevo título como ID del documento

            db.collection("uso_industrias").document(newDocumentId)
                    .set(usoEficienteMap, SetOptions.merge()) // Utilizar SetOptions.merge() para actualizar solo los campos modificados
                    .addOnSuccessListener(aVoid -> {
                        if (documentId != null && !documentId.equals(newDocumentId)) {
                            // Eliminar el documento antiguo si el ID ha cambiado
                            db.collection("uso_industrias").document(documentId)
                                    .delete()
                                    .addOnSuccessListener(aVoid1 -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(CreateUsoIndustriasActivity.this, "Uso eficiente actualizado correctamente", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(CreateUsoIndustriasActivity.this, "Error al eliminar el documento antiguo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateUsoIndustriasActivity.this, "Uso eficiente actualizado correctamente", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateUsoIndustriasActivity.this, "Error al actualizar el documento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Crear un nuevo documento
            db.collection("uso_industrias").document(titulo)
                    .set(usoEficienteMap)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateUsoIndustriasActivity.this, "Uso eficiente creado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateUsoIndustriasActivity.this, "Error al crear el documento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
