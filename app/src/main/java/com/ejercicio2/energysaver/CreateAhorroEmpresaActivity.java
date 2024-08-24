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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.ui.Clase.AhorroEmpresa;
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

public class CreateAhorroEmpresaActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_create_ahorro_empresa);

        // Inicializar vistas
        usoEficienciaPhoto = findViewById(R.id.empresa_photo);
        txtTitulo = findViewById(R.id.txtTitulo_empresa);
        txtFecha = findViewById(R.id.txtFecha_empresa);
        txtDescripcion = findViewById(R.id.txtDescripcion_empresa);
        btnAgregarFoto = findViewById(R.id.btn_agregarphoto_empresa);
        btnEliminarFoto = findViewById(R.id.btn_eliminarphoto_empresa);
        btnGuardar = findViewById(R.id.btnGuardar_empresa);
        progressBar = findViewById(R.id.progressBar_empresa);

        // Inicializar Firebase Firestore y Firebase Storage
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("ahorro_empresa");

        // Establecer fecha actual en el TextView de fecha
        txtFecha.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        // Manejar eventos de los botones
        btnAgregarFoto.setOnClickListener(v -> openFileChooser());
        btnEliminarFoto.setOnClickListener(v -> {
            usoEficienciaPhoto.setImageResource(R.drawable.electrodomestico);
            imageUri = null;
            existingImageUrl = null;
        });
        btnGuardar.setOnClickListener(v -> saveAhorroEmpresa());

        // Verificar si estamos en modo edición
        Intent intent = getIntent();
        if (intent.hasExtra("EXISTING_AHORRO_EMPRESA")) {
            AhorroEmpresa ahorroEmpresa = (AhorroEmpresa) intent.getSerializableExtra("EXISTING_AHORRO_EMPRESA");
            if (ahorroEmpresa != null) {
                isEditing = true;
                oldDocumentId = ahorroEmpresa.getTitulo();
                txtTitulo.setText(ahorroEmpresa.getTitulo());
                txtDescripcion.setText(ahorroEmpresa.getDescripcion());
                txtFecha.setText(ahorroEmpresa.getFecha());
                existingImageUrl = ahorroEmpresa.getImagenUrl();
                if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    Glide.with(this).load(existingImageUrl).into(usoEficienciaPhoto);
                }
                btnGuardar.setText("Actualizar");
            }
        }
    }

    private void openFileChooser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PICK_IMAGE_REQUEST);
            } else {
                openImagePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permiso denegado para acceder a las imágenes", Toast.LENGTH_SHORT).show();
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

    private void saveAhorroEmpresa() {
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
            // Subir la nueva imagen
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Eliminar la imagen antigua si es necesario
                        if (isEditing && existingImageUrl != null && !existingImageUrl.isEmpty()) {
                            StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl);
                            oldImageRef.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Después de eliminar la imagen antigua, guardar los datos actualizados
                                        saveData(titulo, descripcion, fecha, imageUrl);
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(CreateAhorroEmpresaActivity.this, "Error al eliminar la imagen antigua: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // No hay imagen antigua que eliminar
                            saveData(titulo, descripcion, fecha, imageUrl);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateAhorroEmpresaActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No hay nueva imagen, usar la antigua
            saveData(titulo, descripcion, fecha, existingImageUrl);
        }
    }

    private void saveData(String titulo, String descripcion, String fecha, String imageUrl) {
        Map<String, Object> ahorroEmpresaMap = new HashMap<>();
        ahorroEmpresaMap.put("titulo", titulo);
        ahorroEmpresaMap.put("descripcion", descripcion);
        ahorroEmpresaMap.put("fecha", fecha);
        ahorroEmpresaMap.put("imagenUrl", imageUrl);

        if (isEditing) {
            String newDocumentId = titulo;
            if (!newDocumentId.equals(oldDocumentId)) {
                db.collection("ahorro_empresa").document(oldDocumentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> db.collection("ahorro_empresa").document(newDocumentId)
                                .set(ahorroEmpresaMap, SetOptions.merge())
                                .addOnSuccessListener(aVoid1 -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CreateAhorroEmpresaActivity.this, "Ahorro actualizado correctamente", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CreateAhorroEmpresaActivity.this, "Error al actualizar el ahorro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }))
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateAhorroEmpresaActivity.this, "Error al eliminar el ahorro antiguo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                db.collection("ahorro_empresa").document(oldDocumentId)
                        .set(ahorroEmpresaMap, SetOptions.merge())
                        .addOnSuccessListener(aVoid1 -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateAhorroEmpresaActivity.this, "Ahorro actualizado correctamente", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateAhorroEmpresaActivity.this, "Error al actualizar el ahorro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            db.collection("ahorro_empresa").document(titulo)
                    .set(ahorroEmpresaMap, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateAhorroEmpresaActivity.this, "Ahorro agregado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateAhorroEmpresaActivity.this, "Error al agregar el ahorro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }
}
