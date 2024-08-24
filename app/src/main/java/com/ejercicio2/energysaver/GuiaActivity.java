package com.ejercicio2.energysaver;

import android.Manifest;

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

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.ui.Clase.UsoEficiente;

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

public class GuiaActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView usoElectrodomesticoPhoto;
    private EditText txtTitulo, txtDescripcion;
    private TextView txtFecha;
    private Button btnAgregarFoto, btnEliminarFoto, btnGuardar;
    private Uri imageUri;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private boolean isEditing = false;
    private String existingImageUrl;
    private String oldDocumentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guia);

        usoElectrodomesticoPhoto = findViewById(R.id.novedad_photo);
        txtTitulo = findViewById(R.id.txtTitulo);
        txtFecha = findViewById(R.id.txtFecha);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        btnAgregarFoto = findViewById(R.id.btn_agregarphoto_novedad);
        btnEliminarFoto = findViewById(R.id.btn_eliminarphoto_novedad);
        btnGuardar = findViewById(R.id.btnGuardar);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("uso_eficiente");

        txtFecha.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        btnAgregarFoto.setOnClickListener(v -> openFileChooser());

        btnEliminarFoto.setOnClickListener(v -> {
            usoElectrodomesticoPhoto.setImageResource(R.drawable.electrodomestico);
            imageUri = null;
        });

        btnGuardar.setOnClickListener(v -> saveUsoEficiente());

        Intent intent = getIntent();
        if (intent.hasExtra("EXISTING_USO_EFICIENTE")) {
            isEditing = true;
            UsoEficiente usoEficiente = intent.getParcelableExtra("EXISTING_USO_EFICIENTE");
            oldDocumentId = intent.getStringExtra("ID");
            if (usoEficiente != null) {
                txtTitulo.setText(usoEficiente.getTitulo());
                txtDescripcion.setText(usoEficiente.getDescripcion());
                existingImageUrl = usoEficiente.getImagenUrl();
                if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    Glide.with(this).load(existingImageUrl).into(usoElectrodomesticoPhoto);
                }
            }
            btnGuardar.setText("Actualizar");
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                usoElectrodomesticoPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUsoEficiente() {
        String titulo = txtTitulo.getText().toString().trim();
        String descripcion = txtDescripcion.getText().toString().trim();
        String fecha = txtFecha.getText().toString().trim();

        if (TextUtils.isEmpty(titulo)) {
            txtTitulo.setError("Campo obligatorio");
            return;
        }
        if (TextUtils.isEmpty(descripcion)) {
            txtDescripcion.setError("Campo obligatorio");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        if (isEditing && existingImageUrl != null) {
                            deleteOldImageAndSaveData(titulo, descripcion, fecha, imageUrl);
                        } else {
                            saveData(titulo, descripcion, fecha, imageUrl);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(GuiaActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // If no image is selected, use the existing image URL
            saveData(titulo, descripcion, fecha, existingImageUrl);
        }
    }

    private void deleteOldImageAndSaveData(String titulo, String descripcion, String fecha, String newImageUrl) {
        StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl);
        oldImageRef.delete()
                .addOnSuccessListener(aVoid -> saveData(titulo, descripcion, fecha, newImageUrl))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(GuiaActivity.this, "Error al eliminar la imagen antigua: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveData(String titulo, String descripcion, String fecha, String imageUrl) {
        Map<String, Object> usoEficienteMap = new HashMap<>();
        usoEficienteMap.put("titulo", titulo);
        usoEficienteMap.put("descripcion", descripcion);
        usoEficienteMap.put("fecha", fecha);
        usoEficienteMap.put("imagenUrl", imageUrl);

        if (isEditing) {
            String newDocumentId = titulo; // Use the title as the new document ID
            if (!newDocumentId.equals(oldDocumentId)) {
                // Delete the old document and create a new one if the title has changed
                db.collection("uso_eficiente").document(oldDocumentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> db.collection("uso_eficiente").document(newDocumentId)
                                .set(usoEficienteMap, SetOptions.merge()) // Use SetOptions.merge() to update the existing document
                                .addOnSuccessListener(aVoid1 -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(GuiaActivity.this, "Uso eficiente actualizado correctamente", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(GuiaActivity.this, "Error al actualizar el uso eficiente: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }))
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(GuiaActivity.this, "Error al eliminar el documento antiguo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Update the existing document if the title hasn't changed
                db.collection("uso_eficiente").document(oldDocumentId)
                        .set(usoEficienteMap, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(GuiaActivity.this, "Uso eficiente actualizado correctamente", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(GuiaActivity.this, "Error al actualizar el uso eficiente: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            // Create a new document with the title as the ID
            db.collection("uso_eficiente").document(titulo)
                    .set(usoEficienteMap)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(GuiaActivity.this, "Uso eficiente guardado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(GuiaActivity.this, "Error al guardar el uso eficiente: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getFileExtension(Uri uri) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(getContentResolver().getType(uri));
    }
}
