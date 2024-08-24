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
import com.ejercicio2.energysaver.ui.Clase.Calculo;

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

public class CreateCalculoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView calculoPhoto;
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
        setContentView(R.layout.activity_create_calculo);

        // Configurar el padding para los bordes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        inicializarVistas();

        // Inicializar Firebase Firestore y Storage
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("calculos");

        // Configurar botones
        configurarBotones();

        // Configurar la fecha
        configurarFecha();

        // Verificar si estamos editando un cálculo existente
        verificarEdicion();
    }

    private void inicializarVistas() {
        calculoPhoto = findViewById(R.id.calculo_photo);
        txtTitulo = findViewById(R.id.txtTitulo);
        txtFecha = findViewById(R.id.txtFecha);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        btnAgregarFoto = findViewById(R.id.btn_agregarphoto_calculo);
        btnEliminarFoto = findViewById(R.id.btn_eliminarphoto_calculo);
        btnGuardar = findViewById(R.id.btnGuardar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void configurarBotones() {
        btnAgregarFoto.setOnClickListener(v -> openFileChooser());
        btnEliminarFoto.setOnClickListener(v -> eliminarFoto());
        btnGuardar.setOnClickListener(v -> saveCalculo());
    }

    private void configurarFecha() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        txtFecha.setText(currentDate);
    }

    private void verificarEdicion() {
        Intent intent = getIntent();
        if (intent.hasExtra("EXISTING_CALCULO")) {
            isEditing = true;
            Calculo calculo = (Calculo) intent.getSerializableExtra("EXISTING_CALCULO");
            if (calculo != null) {
                oldDocumentId = calculo.getTitulo(); // Usar el título como ID del documento
                txtTitulo.setText(calculo.getTitulo());
                txtDescripcion.setText(calculo.getDescripcion());
                txtFecha.setText(calculo.getFecha());
                existingImageUrl = calculo.getImagenUrl();
                if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    Glide.with(this).load(existingImageUrl).into(calculoPhoto);
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
                calculoPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveCalculo() {
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
            // Eliminar la imagen antigua si está editando
            if (isEditing && existingImageUrl != null && !existingImageUrl.isEmpty()) {
                StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl);
                oldImageRef.delete().addOnSuccessListener(aVoid -> uploadNewImage(titulo, descripcion, fecha))
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateCalculoActivity.this, "Error al eliminar la imagen antigua: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                uploadNewImage(titulo, descripcion, fecha);
            }
        } else {
            // Si no hay nueva imagen, guarda los datos con la URL antigua
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
                    Toast.makeText(CreateCalculoActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveData(String titulo, String descripcion, String fecha, String imageUrl) {
        Map<String, Object> calculoMap = new HashMap<>();
        calculoMap.put("titulo", titulo);
        calculoMap.put("descripcion", descripcion);
        calculoMap.put("fecha", fecha);
        calculoMap.put("imagenUrl", imageUrl);

        if (isEditing) {
            String newDocumentId = titulo;
            if (!newDocumentId.equals(oldDocumentId)) {
                db.collection("calculos").document(oldDocumentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> db.collection("calculos").document(newDocumentId)
                                .set(calculoMap, SetOptions.merge())
                                .addOnSuccessListener(aVoid1 -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CreateCalculoActivity.this, "Cálculo actualizado correctamente", Toast.LENGTH_SHORT).show();
                                    setResult(Activity.RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(CreateCalculoActivity.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }))
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateCalculoActivity.this, "Error al eliminar el documento antiguo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                db.collection("calculos").document(oldDocumentId)
                        .set(calculoMap, SetOptions.merge())
                        .addOnSuccessListener(aVoid1 -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateCalculoActivity.this, "Cálculo actualizado correctamente", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateCalculoActivity.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            db.collection("calculos").document(titulo)
                    .set(calculoMap)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateCalculoActivity.this, "Cálculo agregado correctamente", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateCalculoActivity.this, "Error al agregar cálculo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void eliminarFoto() {
        calculoPhoto.setImageDrawable(null);
        imageUri = null;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
