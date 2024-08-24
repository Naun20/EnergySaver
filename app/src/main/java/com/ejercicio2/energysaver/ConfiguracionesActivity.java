package com.ejercicio2.energysaver;

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
import com.ejercicio2.energysaver.ui.Clase.Configuracion;
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
import android.Manifest;

public class ConfiguracionesActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView configuracionImage;
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
        setContentView(R.layout.activity_configuraciones);

        configuracionImage = findViewById(R.id.configuracion_image);
        txtTitulo = findViewById(R.id.etTitle);
        txtFecha = findViewById(R.id.tvDate);
        txtDescripcion = findViewById(R.id.etDescription);
        btnAgregarFoto = findViewById(R.id.btn_add_config_image);
        btnEliminarFoto = findViewById(R.id.btn_remove_config_image);
        btnGuardar = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("configuraciones_inteligentes");

        txtFecha.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        btnAgregarFoto.setOnClickListener(v -> openFileChooser());

        btnEliminarFoto.setOnClickListener(v -> {
            configuracionImage.setImageResource(R.drawable.logo); // Imagen por defecto
            imageUri = null;
            existingImageUrl = null; // Resetea la URL de la imagen existente si se elimina
        });

        btnGuardar.setOnClickListener(v -> saveConfiguracionInteligente());

        Intent intent = getIntent();
        if (intent.hasExtra("EXISTING_CONFIGURACION")) {
            isEditing = true;
            Configuracion configuracion = intent.getParcelableExtra("EXISTING_CONFIGURACION");
            oldDocumentId = intent.getStringExtra("ID");
            if (configuracion != null) {
                txtTitulo.setText(configuracion.getTitulo());
                txtDescripcion.setText(configuracion.getDescripcion());
                existingImageUrl = configuracion.getImagenUrl();
                if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    Glide.with(this).load(existingImageUrl).into(configuracionImage);
                }
            }
            btnGuardar.setText("Actualizar");
        }
    }

    private void openFileChooser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PICK_IMAGE_REQUEST);
            } else {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
                configuracionImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveConfiguracionInteligente() {
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
            // Si hay una imagen nueva
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        if (isEditing && existingImageUrl != null && !existingImageUrl.isEmpty()) {
                            // Eliminar la imagen antigua
                            StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl);
                            oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                // Imagen antigua eliminada, guardar nueva configuración
                                saveData(titulo, descripcion, fecha, imageUrl);
                            }).addOnFailureListener(e -> {
                                // Error al eliminar la imagen antigua, manejar como sea necesario
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ConfiguracionesActivity.this, "Error al eliminar la imagen antigua: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // No hay imagen antigua para eliminar, simplemente guardar la nueva configuración
                            saveData(titulo, descripcion, fecha, imageUrl);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ConfiguracionesActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No hay nueva imagen, usar la existente
            saveData(titulo, descripcion, fecha, existingImageUrl);
        }
    }

    private void saveData(String titulo, String descripcion, String fecha, String imageUrl) {
        Map<String, Object> configuracionMap = new HashMap<>();
        configuracionMap.put("titulo", titulo);
        configuracionMap.put("descripcion", descripcion);
        configuracionMap.put("fecha", fecha);
        configuracionMap.put("imagenUrl", imageUrl);

        if (isEditing) {
            String newDocumentId = titulo; // Usar el título como el nuevo ID del documento
            if (!newDocumentId.equals(oldDocumentId)) {
                // Eliminar el documento antiguo y crear uno nuevo si el título ha cambiado
                db.collection("configuraciones_inteligentes").document(oldDocumentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> db.collection("configuraciones_inteligentes").document(newDocumentId)
                                .set(configuracionMap, SetOptions.merge()) // Usar SetOptions.merge() para actualizar el documento existente
                                .addOnSuccessListener(aVoid1 -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(ConfiguracionesActivity.this, "Configuración actualizada correctamente", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(ConfiguracionesActivity.this, "Error al actualizar la configuración: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }))
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ConfiguracionesActivity.this, "Error al eliminar el documento antiguo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Actualizar el documento existente si el título no ha cambiado
                db.collection("configuraciones_inteligentes").document(oldDocumentId)
                        .set(configuracionMap, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ConfiguracionesActivity.this, "Configuración actualizada correctamente", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ConfiguracionesActivity.this, "Error al actualizar la configuración: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            // Crear un nuevo documento con el título como ID
            db.collection("configuraciones_inteligentes").document(titulo)
                    .set(configuracionMap)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ConfiguracionesActivity.this, "Configuración guardada correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ConfiguracionesActivity.this, "Error al guardar la configuración: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }
}
