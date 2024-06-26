package com.app.doggyworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    private MeowBottomNavigation bottomNavigation;
    private CategoriaActivity categoriaActivity;
    private ImageButton cartButton, profileButton;
    private TextView titulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cartButton = findViewById(R.id.cartButton);
        profileButton = findViewById(R.id.profileButton);
        titulo = findViewById(R.id.titulo);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        categoriaActivity = new CategoriaActivity(this, recyclerView);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad del carrito
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad del perfil
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Configuración del MeowBottomNavigation
        seleccionarCategoria();
        categoriaActivity.loadProducts("random=4");
    }

    private void seleccionarCategoria() {
        bottomNavigation.add(new MeowBottomNavigation.Model(0, R.drawable.cat_alimentacion));
        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.cat_juguetes));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.cat_paseo));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.cat_higiene));
        bottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.cat_hogar));

        bottomNavigation.show(-1, true);
        bottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                switch (model.getId()) {
                    case 0:
                        categoriaActivity.loadProducts("category=1");
                        titulo.setText("Alimentación");
                        break;
                    case 1:
                        categoriaActivity.loadProducts("category=2");
                        titulo.setText("Juguetes");
                        break;
                    case 2:
                        categoriaActivity.loadProducts("category=3");
                        titulo.setText("Paseo");
                        break;
                    case 3:
                        categoriaActivity.loadProducts("category=4");
                        titulo.setText("Higiene");
                        break;
                    case 4:
                        categoriaActivity.loadProducts("category=5");
                        titulo.setText("Hogar");
                        break;
                }
                return null;
            }
        });
    }
}
