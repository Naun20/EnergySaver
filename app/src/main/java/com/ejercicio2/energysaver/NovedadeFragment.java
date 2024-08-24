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

public class NovedadeFragment extends DialogFragment {

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

    public NovedadeFragment() {
        // Required empty public constructor
    }

    public static NovedadeFragment newInstance() {
        return new NovedadeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("novedad");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_novedade, container, false);

        fotoNovedad = view.findViewById(R.id.novedad_photo);
        txtTitulo = view.findViewById(R.id.txtTitulo);
        txtEnlace = view.findViewById(R.id.txtEnlace);
        txtFecha = view.findViewById(R.id.txtFecha);
        txtDescripcion = view.findViewById(R.id.txtDescripcion);

        btnAgregarFoto = view.findViewById(R.id.btn_agregarphoto_novedad);
        btnEliminarFoto = view.findViewById(R.id.btn_eliminarphoto_novedad);
        btnGuardar = view.findViewById(R.id.btn_guardar);
        progressBar = view.findViewById(R.id.progressBar);

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

        // Recibir datos del argumento
        if (getArguments() != null) {
            txtTitulo.setText(getArguments().getString("titulo"));
            txtDescripcion.setText(getArguments().getString("descripcion"));
            txtEnlace.setText(getArguments().getString("enlace"));
            existingImageUrl = getArguments().getString("imageUrl");
            Glide.with(this).load(existingImageUrl).into(fotoNovedad);
            btnGuardar.setText("Editar");
            isEditing = true;
            tituloEdit = getArguments().getString("titulo");
        }

        return view;
    }

    private void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
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
            Toast.makeText(getContext(), "Debe ingresar un título y seleccionar una imagen.", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getContext(), "Novedad guardada correctamente", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                limpiarCampos();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al guardar la novedad", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            });
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void editarNovedad() {
        String titulo = txtTitulo.getText().toString().trim();
        String enlace = txtEnlace.getText().toString().trim();
        String fecha = txtFecha.getText().toString().trim();
        String descripcion = txtDescripcion.getText().toString().trim();

        if (TextUtils.isEmpty(titulo)) {
            Toast.makeText(getContext(), "Debe ingresar un título.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            StorageReference fileReference = storageRef.child(tituloEdit + "." + getFileExtension(imageUri));

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
                                    Toast.makeText(getContext(), "Novedad actualizada correctamente", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    getActivity().getSupportFragmentManager().popBackStack();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Error al actualizar la novedad", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                });
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
        } else {
            Map<String, Object> novedad = new HashMap<>();
            novedad.put("titulo", titulo);
            novedad.put("enlace", enlace);
            novedad.put("fecha", fecha);
            novedad.put("descripcion", descripcion);
            novedad.put("imageUrl", existingImageUrl);

            db.collection("novedades").document(tituloEdit)
                    .set(novedad, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Novedad actualizada correctamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        getActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al actualizar la novedad", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
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
