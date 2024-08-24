package com.ejercicio2.energysaver;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RestablecerContrasenaActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText correousuario;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restablecer_contrasena);

        mAuth = FirebaseAuth.getInstance();
        correousuario = findViewById(R.id.correousuario);
        progressBar = findViewById(R.id.progressBar);

        // Configurar OnClickListener para el botón de restablecer contraseña
        Button btnRestablecer = findViewById(R.id.btn_login);
        btnRestablecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restablecerContrasena();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Botón para regresar a la actividad anterior
        TextView textRegresar = findViewById(R.id.text_viewregresar);
        textRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Cierra esta actividad para regresar a la anterior
            }
        });
    }

    private void restablecerContrasena() {
        String email = correousuario.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(RestablecerContrasenaActivity.this, "Por favor, ingresa tu correo electrónico.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(RestablecerContrasenaActivity.this, "Se ha enviado un correo de restablecimiento de contraseña.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RestablecerContrasenaActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
