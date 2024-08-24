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
import android.widget.Toast;

import com.bumptech.glide.Glide;

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


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class CreateNovedadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView fotoNovedad;
    private EditText txtTitulo, txtEnlace, txtFecha, txtDescripcion;
    private Button btnAgregarFoto, btnEliminarFoto, btnGuardar;
    private Uri imageUri;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private boolean isEditing = false;
    private String tituloEdit;
    private String existingImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_novedad);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("novedad");

        fotoNovedad = findViewById(R.id.novedad_photo);
        txtTitulo = findViewById(R.id.txtTitulo);
        txtEnlace = findViewById(R.id.txtEnlace);
        txtFecha = findViewById(R.id.txtFecha);
        txtDescripcion = findViewById(R.id.txtDescripcion);

        btnAgregarFoto = findViewById(R.id.btn_agregarphoto_novedad);
        btnEliminarFoto = findViewById(R.id.btn_eliminarphoto_novedad);
        btnGuardar = findViewById(R.id.btn_guardar);
        progressBar = findViewById(R.id.progressBar);

        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        txtFecha.setText(currentDate);

        btnAgregarFoto.setOnClickListener(v -> seleccionarImagen());
        btnEliminarFoto.setOnClickListener(v -> eliminarImagen());
        btnGuardar.setOnClickListener(v -> {
            if (isEditing) {
                editarNovedad();
            } else {
                guardarNovedad();
            }
        });

        // Recibir datos del argumento (si se están pasando desde otra actividad)
        if (getIntent().getExtras() != null) {
            txtTitulo.setText(getIntent().getStringExtra("titulo"));
            txtDescripcion.setText(getIntent().getStringExtra("descripcion"));
            txtEnlace.setText(getIntent().getStringExtra("enlace"));
            existingImageUrl = getIntent().getStringExtra("imageUrl");
            Glide.with(this).load(existingImageUrl).into(fotoNovedad);
            btnGuardar.setText("Editar");
            isEditing = getIntent().getBooleanExtra("isEditing", false); // Obtener el modo de edición
            tituloEdit = getIntent().getStringExtra("titulo");
        }
    }

    private void seleccionarImagen() {
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
        }   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                fotoNovedad.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void eliminarImagen() {
        fotoNovedad.setImageResource(R.drawable.novedades);
        imageUri = null; // Opcional: Restablecer el URI de la imagen a null
    }

    private void guardarNovedad() {
        String titulo = txtTitulo.getText().toString().trim();
        String enlace = txtEnlace.getText().toString().trim();
        String fecha = txtFecha.getText().toString().trim();
        String descripcion = txtDescripcion.getText().toString().trim();

        if (TextUtils.isEmpty(titulo) || imageUri == null) {
            Toast.makeText(this, "Debe ingresar un título y seleccionar una imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        StorageReference fileReference = storageRef.child(titulo + "." + getFileExtension(imageUri));

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    Map<String, Object> novedad = new HashMap<>();
                    novedad.put("titulo", titulo);
                    novedad.put("enlace", enlace);
                    novedad.put("fecha", fecha);
                    novedad.put("descripcion", descripcion);
                    novedad.put("imageUrl", imageUrl);

                    db.collection("novedades").document(titulo)
                            .set(novedad, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Novedad guardada correctamente", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                limpiarCampos();
                                finish(); // Cierra la actividad
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al guardar la novedad", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            });
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }


    private void editarNovedad() {
        String titulo = txtTitulo.getText().toString().trim();
        String enlace = txtEnlace.getText().toString().trim();
        String fecha = txtFecha.getText().toString().trim();
        String descripcion = txtDescripcion.getText().toString().trim();

        if (TextUtils.isEmpty(titulo)) {
            Toast.makeText(this, "Debe ingresar un título.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            StorageReference fileReference = storageRef.child(titulo + "." + getFileExtension(imageUri));

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        Map<String, Object> novedad = new HashMap<>();
                        novedad.put("titulo", titulo);
                        novedad.put("enlace", enlace);
                        novedad.put("fecha", fecha);
                        novedad.put("descripcion", descripcion);
                        novedad.put("imageUrl", imageUrl);

                        db.collection("novedades").document(tituloEdit)
                                .set(novedad, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Novedad actualizada correctamente", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al actualizar la novedad", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                });
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
        } else {
            // Solo actualizar los datos sin cambiar la imagen
            Map<String, Object> novedad = new HashMap<>();
            novedad.put("titulo", titulo);
            novedad.put("enlace", enlace);
            novedad.put("fecha", fecha);
            novedad.put("descripcion", descripcion);
            novedad.put("imageUrl", existingImageUrl); // Mantener la URL existente si no se actualiza la imagen

            db.collection("novedades").document(tituloEdit)
                    .set(novedad, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Novedad actualizada correctamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al actualizar la novedad", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
        }
    }


    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void limpiarCampos() {
        txtTitulo.setText("");
        txtEnlace.setText("");
        txtFecha.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        txtDescripcion.setText("");
        fotoNovedad.setImageResource(R.drawable.novedades);
        imageUri = null;
    }
}
