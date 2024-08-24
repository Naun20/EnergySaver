package com.ejercicio2.energysaver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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


public class LoginAdmActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText correousuario, contrasenaadmin;
    private ProgressBar progressBar, progressBar2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_adm);

        mAuth = FirebaseAuth.getInstance();
        correousuario = findViewById(R.id.correousuario);
        contrasenaadmin = findViewById(R.id.contrasenaadmin);
        progressBar = findViewById(R.id.progressBar);
        progressBar2 = findViewById(R.id.progressBar2);

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isAdmin", true); // true si es admin, false si es un usuario regular
        editor.apply();




        TextView textRegresar = findViewById(R.id.text_viewregresar);
        textRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginAdmActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Cierra la actividad actual para regresar completamente al inicio de sesión inicial
            }
        });

        // Configurar OnClickListener para el TextView de restablecer contraseña
        TextView lblRestablecerCont = findViewById(R.id.lblRestablecerCont);
        lblRestablecerCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Mostrar el ProgressBar
                progressBar2.setVisibility(View.VISIBLE);

                // Iniciar la actividad RestablecerContrasenaActivity después de un pequeño retraso para que el ProgressBar sea visible
                view.postDelayed(() -> {
                    Intent intent = new Intent(LoginAdmActivity.this, RestablecerContrasenaActivity.class);
                    startActivity(intent);
                    progressBar2.setVisibility(View.GONE);
                }, 1000); // 1 segundo de retraso
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAdministrador();
            }
        });
    }


    private void loginAdministrador() {
        String email = correousuario.getText().toString().trim();
        String contrasena = contrasenaadmin.getText().toString().trim();

        if (email.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(LoginAdmActivity.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginAdmActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginAdmActivity.this, MenuActivity.class);
                        startActivity(intent);
                        finish(); // Finaliza la actividad actual
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginAdmActivity.this, "Error en el inicio de sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
