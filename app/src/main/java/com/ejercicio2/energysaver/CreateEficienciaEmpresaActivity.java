package com.ejercicio2.energysaver;

import android.Manifest;
import android.app.Activity;
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
import com.ejercicio2.energysaver.ui.Clase.EficienciaEmpresa;

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

public class CreateEficienciaEmpresaActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView eficienciaEmpresaPhoto;
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
        setContentView(R.layout.activity_create_eficiencia_empresa);

        // Configurar el padding para los bordes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        eficienciaEmpresaPhoto = findViewById(R.id.eficiencia_empresa_photo);
        txtTitulo = findViewById(R.id.txtTitulo);
        txtFecha = findViewById(R.id.txtFecha);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        btnAgregarFoto = findViewById(R.id.btn_agregarphoto_empresa);
        btnEliminarFoto = findViewById(R.id.btn_eliminarphoto_empresa);
        btnGuardar = findViewById(R.id.btnGuardar);
        progressBar = findViewById(R.id.progressBar);

        // Inicializar Firebase Firestore y Firebase Storage
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("eficiencia_empresa");

        // Establecer fecha actual en el TextView de fecha
        txtFecha.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        // Manejar eventos de los botones
        btnAgregarFoto.setOnClickListener(v -> openFileChooser());
        btnEliminarFoto.setOnClickListener(v -> {
            eficienciaEmpresaPhoto.setImageResource(R.drawable.electrodomestico);
            imageUri = null;
        });
        btnGuardar.setOnClickListener(v -> saveEficienciaEmpresa());

        // Verificar si estamos en modo edición
        Intent intent = getIntent();
        if (intent.hasExtra("EXISTING_EFICIENCIA_EMPRESA")) {
            EficienciaEmpresa eficiencia = (EficienciaEmpresa) intent.getSerializableExtra("EXISTING_EFICIENCIA_EMPRESA");
            if (eficiencia != null) {
                isEditing = true;
                oldDocumentId = eficiencia.getTitulo(); // Usar el título como ID del documento
                txtTitulo.setText(eficiencia.getTitulo());
                txtDescripcion.setText(eficiencia.getDescripcion());
                txtFecha.setText(eficiencia.getFecha());
                existingImageUrl = eficiencia.getImagenUrl();
                if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    Glide.with(this).load(existingImageUrl).into(eficienciaEmpresaPhoto);
                }
                btnGuardar.setText("Actualizar");
            }
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
                eficienciaEmpresaPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveEficienciaEmpresa() {
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
            // Eliminar la imagen antigua si estamos en modo edición
            if (isEditing && existingImageUrl != null) {
                StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl);
                oldImageRef.delete().addOnSuccessListener(aVoid -> uploadNewImage(titulo, descripcion, fecha))
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateEficienciaEmpresaActivity.this, "Error al eliminar la imagen antigua: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                uploadNewImage(titulo, descripcion, fecha);
            }
        } else {
            saveData(titulo, descripcion, fecha, existingImageUrl);
        }
    }

    private void uploadNewImage(String titulo, String descripcion, String fecha) {
        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveData(titulo, descripcion, fecha, imageUrl);
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateEficienciaEmpresaActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveData(String titulo, String descripcion, String fecha, String imageUrl) {
        Map<String, Object> eficienciaEmpresaMap = new HashMap<>();
        eficienciaEmpresaMap.put("titulo", titulo);
        eficienciaEmpresaMap.put("descripcion", descripcion);
        eficienciaEmpresaMap.put("fecha", fecha);
        eficienciaEmpresaMap.put("imagenUrl", imageUrl);

        if (isEditing) {
            String newDocumentId = titulo;
            if (!newDocumentId.equals(oldDocumentId)) {
                db.collection("eficiencia_empresa").document(oldDocumentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> db.collection("eficiencia_empresa").document(newDocumentId)
                                .set(eficienciaEmpresaMap, SetOptions.merge())
                                .addOnSuccessListener(aVoid1 -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CreateEficienciaEmpresaActivity.this, "Eficiencia en Empresa actualizada correctamente", Toast.LENGTH_SHORT).show();
                                    setResult(Activity.RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CreateEficienciaEmpresaActivity.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }))
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateEficienciaEmpresaActivity.this, "Error al eliminar el documento antiguo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                db.collection("eficiencia_empresa").document(oldDocumentId)
                        .set(eficienciaEmpresaMap, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateEficienciaEmpresaActivity.this, "Eficiencia en Empresa actualizada correctamente", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateEficienciaEmpresaActivity.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            db.collection("eficiencia_empresa").document(titulo)
                    .set(eficienciaEmpresaMap, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateEficienciaEmpresaActivity.this, "Eficiencia en Empresa guardada correctamente", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateEficienciaEmpresaActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
