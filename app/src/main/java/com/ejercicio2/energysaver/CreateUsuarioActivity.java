package com.ejercicio2.energysaver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.ejercicio2.energysaver.ui.Clase.Usuario;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import android.Manifest;

public class CreateUsuarioActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextInputEditText nombre, nombreusuario, TxtDni, TxtTelefono, contrasenaadmin, TxtEmail;
    private Spinner spinnerTipoUsuario;
    private Button btnEliminarFoto, btnEditar, btnGuardarUsuario;
    private ImageView fotoNegocio;
    private ProgressBar progressBar;
    private Uri imageUri;
    private static final String IMAGEN_DEFECTO_URL = "https://firebasestorage.googleapis.com/v0/b/energysaver-dc0a3.appspot.com/o/default_image.png?alt=media&token=5342751d-e0f5-46fc-8687-bd157008267d";

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;

    private String userId;
    private String imageUrl; // URL de la imagen actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_usuario);

        nombre = findViewById(R.id.TxtNombre);
        nombreusuario = findViewById(R.id.usuario);
        TxtDni = findViewById(R.id.TxtDni);
        TxtTelefono = findViewById(R.id.TxtTelefono);
        TxtEmail = findViewById(R.id.TxtEmail); // Inicialización añadida
        spinnerTipoUsuario = findViewById(R.id.spinnerTipoUsuariouser);
        btnEliminarFoto = findViewById(R.id.btn_eliminarphoto);
        btnEditar = findViewById(R.id.btn_agregarphoto);
        btnGuardarUsuario = findViewById(R.id.btn_addUser);
        fotoNegocio = findViewById(R.id.usuario_photo);
        progressBar = findViewById(R.id.progressBar);
        contrasenaadmin = findViewById(R.id.contrasenaadmin); // Para la contraseña

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.admin_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoUsuario.setAdapter(adapter);

        // Verifica si hay un usuario existente para editar
        Intent intent = getIntent();
        if (intent.hasExtra("EXISTING_USUARIO")) {
            Usuario usuario = (Usuario) intent.getSerializableExtra("EXISTING_USUARIO");

            // Llenar los campos con los datos del usuario existente
            nombre.setText(usuario.getNombreCompleto());
            nombreusuario.setText(usuario.getNombreUsuario());
            TxtDni.setText(usuario.getDni());
            TxtTelefono.setText(usuario.getTelefono());
            contrasenaadmin.setText(usuario.getContrasena());
            TxtEmail.setText(usuario.getEmail()); // Añadir el email

            // Cargar la imagen existente (si hay una URL)
            if (usuario.getImageUrl() != null && !usuario.getImageUrl().isEmpty()) {
                Glide.with(this).load(usuario.getImageUrl()).into(fotoNegocio);
                imageUrl = usuario.getImageUrl(); // Mantener la URL de la imagen existente
            }

            // Seleccionar el tipo de usuario en el spinner
            int position = adapter.getPosition(usuario.getTipoUsuario());
            spinnerTipoUsuario.setSelection(position);

            // Cambiar el texto del botón
            btnGuardarUsuario.setText("Actualizar Usuario");


        }

        // Configuración de los botones
        fotoNegocio.setOnClickListener(v -> seleccionarImagen());
        btnEditar.setOnClickListener(v -> seleccionarImagen());
        btnEliminarFoto.setOnClickListener(v -> eliminarImagen());
        btnGuardarUsuario.setOnClickListener(v -> {
            if (btnGuardarUsuario.getText().toString().equals("Guardar Usuario")) {
                guardarUsuario();
            } else {
                actualizarUsuario();
            }
        });
    }
    private void seleccionarImagen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Verifica el permiso para acceder a imágenes en dispositivos con Android 13 y superior
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // Solicita el permiso si no está concedido
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, PICK_IMAGE_REQUEST);
            } else {
                // Si el permiso ya está concedido, abre el selector de imágenes
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
            }
        } else {
            // Para versiones anteriores a Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Solicita el permiso si no está concedido
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            } else {
                // Si el permiso ya está concedido, abre el selector de imágenes
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, ahora puedes abrir el selector de imágenes
                seleccionarImagen();
            } else {
                // Permiso denegado, muestra un mensaje al usuario
                Toast.makeText(this, "Permiso para acceder a imágenes denegado", Toast.LENGTH_SHORT).show();
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
                fotoNegocio.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void eliminarImagen() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            imageRef.delete().addOnSuccessListener(aVoid -> {
                fotoNegocio.setImageResource(R.drawable.usuario); // Asegúrate de que este recurso exista
                imageUri = null;
                imageUrl = null; // Limpiar la URL
            }).addOnFailureListener(e -> {
                Toast.makeText(CreateUsuarioActivity.this, "Error al eliminar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            fotoNegocio.setImageResource(R.drawable.usuario); // Asegúrate de que este recurso exista
            imageUri = null;
            imageUrl = null; // Limpiar la URL
        }
    }

    private void guardarUsuario() {
        String nombreCompleto = nombre.getText().toString();
        String nombreUsuario = nombreusuario.getText().toString();
        String email = TxtEmail.getText().toString(); // Corregido aquí
        String dni = TxtDni.getText().toString();
        String telefono = TxtTelefono.getText().toString();
        String tipoUsuario = spinnerTipoUsuario.getSelectedItem().toString();
        String contrasena = contrasenaadmin.getText().toString(); // Leer la contraseña

        if (nombreCompleto.isEmpty() || nombreUsuario.isEmpty() || email.isEmpty() || dni.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos excepto la imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnGuardarUsuario.getText().toString().equals("Guardar Usuario")) {
            if (contrasena.isEmpty()) {
                Toast.makeText(this, "La contraseña es obligatoria para crear un nuevo usuario.", Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);

            auth.createUserWithEmailAndPassword(email, contrasena)
                    .addOnSuccessListener(authResult -> {
                        String userId = authResult.getUser().getUid();
                        if (imageUri != null) {
                            subirImagen(userId, nombreCompleto, email, nombreUsuario, contrasena, dni, telefono, tipoUsuario);
                        } else {
                            guardarDatosUsuarioSinImagen(userId, nombreCompleto, nombreUsuario, email, contrasena, dni, telefono, tipoUsuario);
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateUsuarioActivity.this, "Error al crear el usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            actualizarUsuario();
        }
    }

    private void actualizarUsuario() {
        String nombreCompleto = nombre.getText().toString();
        String nombreUsuario = nombreusuario.getText().toString();
        String email = TxtEmail.getText().toString(); // Añade esta línea para recoger el email
        String dni = TxtDni.getText().toString();
        String telefono = TxtTelefono.getText().toString();
        String tipoUsuario = spinnerTipoUsuario.getSelectedItem().toString();
        String contrasena = contrasenaadmin.getText().toString();

        if (nombreCompleto.isEmpty() || nombreUsuario.isEmpty() || email.isEmpty() || dni.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("nombreCompleto", nombreCompleto);
            userMap.put("nombreUsuario", nombreUsuario);
            userMap.put("email", email); // Asegúrate de incluir el email aquí
            userMap.put("dni", dni);
            userMap.put("telefono", telefono);
            userMap.put("tipoUsuario", tipoUsuario);
            if (!contrasena.isEmpty()) {
                userMap.put("contrasena", contrasena);
            }

            // Actualiza los datos del usuario en Firestore
            db.collection("usuarios").document(userId).update(userMap)
                    .addOnSuccessListener(aVoid -> {
                        if (imageUri != null) {
                            // Si hay una nueva imagen, sube la imagen y actualiza la URL en Firestore
                            subirImagen(userId, nombreCompleto, email, nombreUsuario, contrasena, dni, telefono, tipoUsuario);
                        } else {
                            // Si no hay nueva imagen, solo actualiza el usuario sin cambiar la URL de la imagen
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateUsuarioActivity.this, "Usuario actualizado con éxito", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateUsuarioActivity.this, "Error al actualizar el usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void subirImagen(String userId, String nombreCompleto, String email, String nombreUsuario, String contrasena, String dni, String telefono, String tipoUsuario) {
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("user_images/" + UUID.randomUUID().toString());

        fotoNegocio.setDrawingCacheEnabled(true);
        fotoNegocio.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) fotoNegocio.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            guardarDatosUsuarioConImagen(userId, nombreCompleto, nombreUsuario, email, contrasena, dni, telefono, tipoUsuario, imageUrl);
        })).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(CreateUsuarioActivity.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void guardarDatosUsuarioConImagen(String userId, String nombreCompleto, String nombreUsuario, String email, String contrasena, String dni, String telefono, String tipoUsuario, String imageUrl) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nombreCompleto", nombreCompleto);
        userMap.put("nombreUsuario", nombreUsuario);
        userMap.put("email", email);
        userMap.put("contrasena", contrasena);
        userMap.put("dni", dni);
        userMap.put("telefono", telefono);
        userMap.put("tipoUsuario", tipoUsuario);
        userMap.put("imageUrl", imageUrl);

        db.collection("usuarios").document(userId).set(userMap)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateUsuarioActivity.this, "Usuario creado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateUsuarioActivity.this, "Error al crear el usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void guardarDatosUsuarioSinImagen(String userId, String nombreCompleto, String nombreUsuario, String email, String contrasena, String dni, String telefono, String tipoUsuario) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nombreCompleto", nombreCompleto);
        userMap.put("nombreUsuario", nombreUsuario);
        userMap.put("email", email);
        userMap.put("contrasena", contrasena);
        userMap.put("dni", dni);
        userMap.put("telefono", telefono);
        userMap.put("tipoUsuario", tipoUsuario);
        userMap.put("imageUrl", IMAGEN_DEFECTO_URL); // Agrega la URL de la imagen por defecto

        db.collection("usuarios").document(userId).set(userMap)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateUsuarioActivity.this, "Usuario creado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateUsuarioActivity.this, "Error al crear el usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
