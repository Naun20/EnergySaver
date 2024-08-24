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
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.UUID;

public class CreateNoticiaActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText txtTitulo, txtDescripcion, txtEnlace, txtFecha;
    private Button btnGuardar, btnAgregarPhoto, btnEliminarPhoto;
    private ImageView noticiaPhoto;
    private Uri imageUri;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    private String oldTitle;
    private String documentId;
    private String existingImageUrl;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_noticia);

        txtTitulo = findViewById(R.id.txtTitulo);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        txtEnlace = findViewById(R.id.txtEnlace);
        txtFecha = findViewById(R.id.txtFecha);
        btnGuardar = findViewById(R.id.btn_guardarnoticia);
        btnAgregarPhoto = findViewById(R.id.btn_agregarphoto_noticia);
        btnEliminarPhoto = findViewById(R.id.btn_eliminarphoto_noticia);
        noticiaPhoto = findViewById(R.id.noticia_photo);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("noticias");

        txtFecha.setText(getCurrentDate());

        btnAgregarPhoto.setOnClickListener(v -> openFileChooser());

        btnEliminarPhoto.setOnClickListener(v -> {
            noticiaPhoto.setImageResource(R.drawable.baseline_co_present_24);
            imageUri = null;
        });

        btnGuardar.setOnClickListener(v -> {
            if (isEditMode) {
                updateNoticia();
            } else {
                saveNoticia();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            documentId = intent.getStringExtra("documentId");
            isEditMode = documentId != null;
            if (isEditMode) {
                btnGuardar.setText("Editar");
                loadNoticiaData(documentId); // Cargar los datos de la noticia
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                noticiaPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void saveNoticia() {
        final String titulo = txtTitulo.getText().toString().trim();
        final String descripcion = txtDescripcion.getText().toString().trim();
        final String enlace = txtEnlace.getText().toString().trim();
        final String fecha = txtFecha.getText().toString().trim();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(enlace) || imageUri == null) {
            Toast.makeText(this, "Por favor, complete todos los campos y seleccione una imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Generar un ID único para la imagen
        final String imageId = UUID.randomUUID().toString();
        final StorageReference fileReference = storageRef.child(imageId + "." + getFileExtension(imageUri));
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    Map<String, Object> noticia = new HashMap<>();
                    noticia.put("titulo", titulo);
                    noticia.put("descripcion", descripcion);
                    noticia.put("enlace", enlace);
                    noticia.put("fecha", fecha);
                    noticia.put("imagenUrl", imageUrl);

                    db.collection("noticias").document(titulo)
                            .set(noticia)
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Noticia guardada exitosamente.", Toast.LENGTH_SHORT).show();
                                clearFields();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Error al guardar la noticia.", Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al subir la imagen.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateNoticia() {
        final String nuevoTitulo = txtTitulo.getText().toString().trim();
        final String descripcion = txtDescripcion.getText().toString().trim();
        final String enlace = txtEnlace.getText().toString().trim();
        final String fecha = txtFecha.getText().toString().trim();

        if (TextUtils.isEmpty(nuevoTitulo) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(enlace)) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            // Si hay una nueva imagen, primero eliminar la antigua
            if (existingImageUrl != null) {
                // Obtener la referencia a la imagen antigua
                StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUrl);
                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                    // Imagen antigua eliminada con éxito, ahora subir la nueva
                    uploadNewImageAndUpdateNoticia(nuevoTitulo, descripcion, enlace, fecha);
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al eliminar la imagen antigua.", Toast.LENGTH_SHORT).show();
                });
            } else {
                // No hay imagen antigua, solo subir la nueva
                uploadNewImageAndUpdateNoticia(nuevoTitulo, descripcion, enlace, fecha);
            }
        } else {
            // No se ha elegido una nueva imagen, actualizar sin cambiar el URL
            updateNoticiaInFirestore(nuevoTitulo, descripcion, enlace, fecha, existingImageUrl);
        }
    }

    private void uploadNewImageAndUpdateNoticia(String nuevoTitulo, String descripcion, String enlace, String fecha) {
        final String imageId = UUID.randomUUID().toString();
        final StorageReference fileReference = storageRef.child(imageId + "." + getFileExtension(imageUri));
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    updateNoticiaInFirestore(nuevoTitulo, descripcion, enlace, fecha, imageUrl);
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al subir la nueva imagen.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateNoticiaInFirestore(String nuevoTitulo, String descripcion, String enlace, String fecha, String imageUrl) {
        Map<String, Object> noticia = new HashMap<>();
        noticia.put("titulo", nuevoTitulo);
        noticia.put("descripcion", descripcion);
        noticia.put("enlace", enlace);
        noticia.put("fecha", fecha);
        noticia.put("imagenUrl", imageUrl);

        // Si el título ha cambiado, eliminar el documento antiguo y crear uno nuevo
        if (!nuevoTitulo.equals(oldTitle)) {
            db.collection("noticias").document(oldTitle)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        db.collection("noticias").document(nuevoTitulo)
                                .set(noticia, SetOptions.merge())
                                .addOnSuccessListener(aVoid1 -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Noticia actualizada exitosamente.", Toast.LENGTH_SHORT).show();
                                    finish(); // Cierra la actividad
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Error al actualizar la noticia.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error al eliminar el documento antiguo.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Si el título no ha cambiado, solo actualizar el documento existente
            db.collection("noticias").document(nuevoTitulo)
                    .set(noticia, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Noticia actualizada exitosamente.", Toast.LENGTH_SHORT).show();
                        finish(); // Cierra la actividad
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error al actualizar la noticia.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadNoticiaData(String documentId) {
        db.collection("noticias").document(documentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        oldTitle = documentSnapshot.getString("titulo");
                        String descripcion = documentSnapshot.getString("descripcion");
                        String enlace = documentSnapshot.getString("enlace");
                        String fecha = documentSnapshot.getString("fecha");
                        existingImageUrl = documentSnapshot.getString("imagenUrl");

                        txtTitulo.setText(oldTitle);
                        txtDescripcion.setText(descripcion);
                        txtEnlace.setText(enlace);
                        txtFecha.setText(fecha);

                        if (existingImageUrl != null) {
                            Glide.with(this).load(existingImageUrl).into(noticiaPhoto);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar los datos de la noticia.", Toast.LENGTH_SHORT).show());
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }

    private void clearFields() {
        txtTitulo.setText("");
        txtDescripcion.setText("");
        txtEnlace.setText("");
        txtFecha.setText(getCurrentDate());
        noticiaPhoto.setImageResource(R.drawable.baseline_co_present_24);
        imageUri = null;
    }
}
