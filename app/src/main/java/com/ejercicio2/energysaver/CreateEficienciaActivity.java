package com.ejercicio2.energysaver;

import android.Manifest;
import android.app.Activity;
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
import com.ejercicio2.energysaver.ui.Clase.Eficiencia;

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

public class CreateEficienciaActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView usoEficienciaPhoto;
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
        setContentView(R.layout.activity_create_eficiencia);

        usoEficienciaPhoto = findViewById(R.id.novedad_photo);
        txtTitulo = findViewById(R.id.txtTitulo);
        txtFecha = findViewById(R.id.txtFecha);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        btnAgregarFoto = findViewById(R.id.btn_agregarphoto_eficiencia);
        btnEliminarFoto = findViewById(R.id.btn_eliminarphoto_eficiencia);
        btnGuardar = findViewById(R.id.btnGuardar);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("eficiencia_energetica");

        txtFecha.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        btnAgregarFoto.setOnClickListener(v -> openFileChooser());

        btnEliminarFoto.setOnClickListener(v -> {
            usoEficienciaPhoto.setImageResource(R.drawable.electrodomestico);
            imageUri = null;
        });

        btnGuardar.setOnClickListener(v -> saveEficiencia());

        Intent intent = getIntent();
        if (intent.hasExtra("EXISTING_EFICIENCIA")) {
            Eficiencia eficiencia = (Eficiencia) intent.getSerializableExtra("EXISTING_EFICIENCIA");
            if (eficiencia != null) {
                isEditing = true;
                oldDocumentId = eficiencia.getTitulo();
                txtTitulo.setText(eficiencia.getTitulo());
                txtDescripcion.setText(eficiencia.getDescripcion());
                existingImageUrl = eficiencia.getImagenUrl();
                if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    Glide.with(this).load(existingImageUrl).into(usoEficienciaPhoto);
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
                usoEficienciaPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveEficiencia() {
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
                        deleteOldImageAndSaveData(titulo, descripcion, fecha, imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateEficienciaActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Si no se selecciona imagen, usa la URL de imagen existente
            saveData(titulo, descripcion, fecha, existingImageUrl);
        }
    }

    private void deleteOldImageAndSaveData(String titulo, String descripcion, String fecha, String newImageUrl) {
        if (isEditing && existingImageUrl != null) {
            StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl);
            oldImageRef.delete().addOnSuccessListener(aVoid -> {
                // Old image deleted, now save new data
                saveData(titulo, descripcion, fecha, newImageUrl);
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateEficienciaActivity.this, "Error al eliminar la imagen antigua: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // No old image to delete
            saveData(titulo, descripcion, fecha, newImageUrl);
        }
    }

    private void saveData(String titulo, String descripcion, String fecha, String imageUrl) {
        Map<String, Object> eficienciaMap = new HashMap<>();
        eficienciaMap.put("titulo", titulo);
        eficienciaMap.put("descripcion", descripcion);
        eficienciaMap.put("fecha", fecha);
        eficienciaMap.put("imagenUrl", imageUrl);

        if (isEditing) {
            String newDocumentId = titulo;
            if (!newDocumentId.equals(oldDocumentId)) {
                db.collection("eficiencia_energetica").document(oldDocumentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> db.collection("eficiencia_energetica").document(newDocumentId)
                                .set(eficienciaMap, SetOptions.merge())
                                .addOnSuccessListener(aVoid1 -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CreateEficienciaActivity.this, "Eficiencia actualizada correctamente", Toast.LENGTH_SHORT).show();
                                    setResult(Activity.RESULT_OK); // Indica éxito
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CreateEficienciaActivity.this, "Error al actualizar eficiencia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }))
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateEficienciaActivity.this, "Error al eliminar documento antiguo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Solo actualizar el documento existente
                db.collection("eficiencia_energetica").document(oldDocumentId)
                        .set(eficienciaMap, SetOptions.merge())
                        .addOnSuccessListener(aVoid1 -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateEficienciaActivity.this, "Eficiencia actualizada correctamente", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK); // Indica éxito
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateEficienciaActivity.this, "Error al actualizar eficiencia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            // Crear un nuevo documento
            db.collection("eficiencia_energetica").document(titulo)
                    .set(eficienciaMap)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateEficienciaActivity.this, "Eficiencia creada correctamente", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK); // Indica éxito
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateEficienciaActivity.this, "Error al crear eficiencia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getFileExtension(Uri uri) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(getContentResolver().getType(uri));
    }
}
