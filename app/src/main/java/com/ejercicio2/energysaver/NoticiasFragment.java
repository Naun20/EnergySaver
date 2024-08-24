package com.ejercicio2.energysaver;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


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
import androidx.fragment.app.DialogFragment;

public class NoticiasFragment extends DialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText txtTitulo, txtDescripcion, txtEnlace, txtFecha;
    private Button btnGuardar, btnAgregarPhoto, btnEliminarPhoto;
    private ImageView noticiaPhoto;
    private Uri imageUri;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    private String documentId;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_noticias, container, false);

        txtTitulo = view.findViewById(R.id.txtTitulo);
        txtDescripcion = view.findViewById(R.id.txtDescripcion);
        txtEnlace = view.findViewById(R.id.txtEnlace);
        txtFecha = view.findViewById(R.id.txtFecha);
        btnGuardar = view.findViewById(R.id.btn_guardarnoticia);
        btnAgregarPhoto = view.findViewById(R.id.btn_agregarphoto_noticia);
        btnEliminarPhoto = view.findViewById(R.id.btn_eliminarphoto_noticia);
        noticiaPhoto = view.findViewById(R.id.noticia_photo);
        progressBar = view.findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("noticias");

        txtFecha.setText(getCurrentDate());

        btnAgregarPhoto.setOnClickListener(v -> openFileChooser());

        btnEliminarPhoto.setOnClickListener(v -> {
            imageUri = null;
            noticiaPhoto.setImageResource(R.drawable.baseline_co_present_24);
        });

        btnGuardar.setOnClickListener(v -> {
            if (isEditMode) {
                updateNoticia();
            } else {
                saveNoticia();
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            documentId = args.getString("documentId");
            isEditMode = true;
            btnGuardar.setText("Editar");
            loadNoticiaData(documentId);
        }

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                noticiaPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
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
            Toast.makeText(getContext(), "Por favor, complete todos los campos y seleccione una imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        final StorageReference fileReference = storageRef.child(titulo + "." + getFileExtension(imageUri));
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
                                Toast.makeText(getContext(), "Noticia guardada exitosamente.", Toast.LENGTH_SHORT).show();
                                clearFields();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Error al guardar la noticia.", Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al subir la imagen.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateNoticia() {
        final String titulo = txtTitulo.getText().toString().trim();
        final String descripcion = txtDescripcion.getText().toString().trim();
        final String enlace = txtEnlace.getText().toString().trim();
        final String fecha = txtFecha.getText().toString().trim();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(enlace)) {
            Toast.makeText(getContext(), "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> noticia = new HashMap<>();
        noticia.put("titulo", titulo);
        noticia.put("descripcion", descripcion);
        noticia.put("enlace", enlace);
        noticia.put("fecha", fecha);

        if (imageUri != null) {
            final StorageReference fileReference = storageRef.child(titulo + "." + getFileExtension(imageUri));
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        noticia.put("imagenUrl", imageUrl);

                        db.collection("noticias").document(documentId)
                                .set(noticia, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Noticia actualizada exitosamente.", Toast.LENGTH_SHORT).show();
                                    getActivity().onBackPressed(); // Cierra el fragmento
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Error al actualizar la noticia.", Toast.LENGTH_SHORT).show();
                                });
                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error al subir la imagen.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("noticias").document(documentId)
                    .set(noticia, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Noticia actualizada exitosamente.", Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed(); // Cierra el fragmento
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error al actualizar la noticia.", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void loadNoticiaData(String documentId) {
        db.collection("noticias").document(documentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String titulo = documentSnapshot.getString("titulo");
                        String descripcion = documentSnapshot.getString("descripcion");
                        String enlace = documentSnapshot.getString("enlace");
                        String fecha = documentSnapshot.getString("fecha");
                        String imagenUrl = documentSnapshot.getString("imagenUrl");

                        txtTitulo.setText(titulo);
                        txtDescripcion.setText(descripcion);
                        txtEnlace.setText(enlace);
                        txtFecha.setText(fecha);

                        if (imagenUrl != null && !imagenUrl.isEmpty()) {
                            Glide.with(this).load(imagenUrl).into(noticiaPhoto);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al cargar los datos de la noticia.", Toast.LENGTH_SHORT).show());
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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
