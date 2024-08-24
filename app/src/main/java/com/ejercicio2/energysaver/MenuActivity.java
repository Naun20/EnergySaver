package com.ejercicio2.energysaver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ejercicio2.energysaver.databinding.ActivityMenuBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class MenuActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMenuBinding binding;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance(); // Inicializar FirebaseAuth

        setSupportActionBar(binding.appBarMenu.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Añadir todos los fragmentos al AppBarConfiguration
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.nav_eficiencia,
                R.id.nav_ahorro,
                R.id.nav_uso_industria,
                R.id.nav_eficiencia_industria,
                R.id.nav_empresa,
                R.id.nav_ahorroEmpresa)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Actualizar el NavigationView con la información del usuario
        View headerView = navigationView.getHeaderView(0);
        TextView lblEmailRegis = headerView.findViewById(R.id.lblEmailRegis);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            lblEmailRegis.setText(currentUser.getEmail());

            // Obtener el menú del NavigationView
            Menu menu = navigationView.getMenu();

            // Verificar el providerId del usuario
            boolean isEmailProvider = false;
            for (UserInfo profile : currentUser.getProviderData()) {
                if (EmailAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                    isEmailProvider = true;
                    break;
                }
            }

            // Mostrar u ocultar el ítem nav_usuario basado en el providerId
            menu.findItem(R.id.nav_usuario).setVisible(isEmailProvider);
        } else {
            lblEmailRegis.setText("No autenticado");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_close) {
            logout(); // Llamar al método de cierre de sesión
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Restablecer el estado de autenticación
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Cerrar sesión en Firebase
        FirebaseAuth.getInstance().signOut();

        // Mostrar el mensaje de cierre de sesión correcto
        Toast.makeText(MenuActivity.this, "Sesión cerrada correctamente.", Toast.LENGTH_SHORT).show();

        // Redirigir a la pantalla de inicio de sesión
        Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
