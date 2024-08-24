package com.ejercicio2.energysaver.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.ejercicio2.energysaver.CreateNoticiaActivity;
import com.ejercicio2.energysaver.CreateNovedadActivity;
import com.ejercicio2.energysaver.R;
import com.ejercicio2.energysaver.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ViewPager2 viewPager;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private HomeViewModel homeViewModel;
    private FirebaseAuth auth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewPager = root.findViewById(R.id.viewPager);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewnoticia);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Verificar el estado de autenticación y el rol del usuario
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        this.auth = FirebaseAuth.getInstance();

        // Configurar el TextView para mostrar el PopupWindow
        binding.textViewconsulta.setOnClickListener(this::showFilterPopup);

        // Verifica si el usuario está autenticado por correo
        if (auth.getCurrentUser() != null && "password".equals(auth.getCurrentUser().getProviderData().get(1).getProviderId())) {
            binding.textViewconsulta.setVisibility(View.VISIBLE);
        } else {
            binding.textViewconsulta.setVisibility(View.GONE);
        }

        homeViewModel.getNovedades().observe(getViewLifecycleOwner(), novedades -> {
            CardAdapter adapter = new CardAdapter(getContext(), novedades);
            viewPager.setAdapter(adapter);
        });

        homeViewModel.getNoticias().observe(getViewLifecycleOwner(), noticias -> {
            NoticiaAdapter adapter = new NoticiaAdapter(getContext(), noticias);
            recyclerView.setAdapter(adapter);
        });

        // Agregar el OnScrollListener al RecyclerView con animación
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (dy > 0 && viewPager.getVisibility() == View.VISIBLE) {
                        fadeOut(viewPager);
                    } else if (dy < 0 && firstVisibleItemPosition == 0 && viewPager.getVisibility() == View.GONE) {
                        fadeIn(viewPager);
                    }
                }
            }
        });

        runnable = () -> {
            int currentItem = viewPager.getCurrentItem();
            int nextItem = currentItem + 1 == viewPager.getAdapter().getItemCount() ? 0 : currentItem + 1;
            viewPager.setCurrentItem(nextItem, true);
            handler.postDelayed(runnable, 10000); // 10 segundos
        };

        handler.postDelayed(runnable, 10000); // 10 segundos

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
        binding = null;
    }

    private void fadeOut(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(fadeOut);
    }

    private void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(300);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(fadeIn);
    }

    private void showFilterPopup(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_add, null);

        // Crear PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        // Configurar el contenido del PopupWindow
        TextView option1 = popupView.findViewById(R.id.popup_option1);
        TextView option2 = popupView.findViewById(R.id.popup_option2);

        option1.setOnClickListener(v -> {
            // Iniciar CreateNovedadActivity
            Intent intent = new Intent(getActivity(), CreateNovedadActivity.class);
            startActivity(intent);
            popupWindow.dismiss();
        });

        option2.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateNoticiaActivity.class);
            startActivity(intent);
            popupWindow.dismiss();
        });

        // Mostrar el PopupWindow
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, location[0], location[1] + anchorView.getHeight());
    }
}
