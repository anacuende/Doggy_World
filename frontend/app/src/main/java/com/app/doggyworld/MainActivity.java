package com.app.doggyworld;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    private MeowBottomNavigation bottomNavigation;
    private CategoriaActivity categoriaActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        categoriaActivity = new CategoriaActivity(this, recyclerView);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        seleccionarCategoria();
        categoriaActivity.loadProducts("random=3");
    }

    private void seleccionarCategoria() {
        bottomNavigation.add(new MeowBottomNavigation.Model(0, R.drawable.icono_doggy_world));
        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.icono_doggy_world));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.icono_doggy_world));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.icono_doggy_world));
        bottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.icono_doggy_world));
        bottomNavigation.add(new MeowBottomNavigation.Model(5, R.drawable.icono_doggy_world));

        bottomNavigation.show(0, true);
        bottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                switch (model.getId()) {
                    case 0:
                        categoriaActivity.loadProducts("random=3");
                        break;
                    case 1:
                        categoriaActivity.loadProducts("category=1");
                        break;
                    case 2:
                        categoriaActivity.loadProducts("category=2");
                        break;
                    case 3:
                        categoriaActivity.loadProducts("category=3");
                        break;
                    case 4:
                        categoriaActivity.loadProducts("category=4");
                        break;
                    case 5:
                        categoriaActivity.loadProducts("category=5");
                        break;
                }
                return null;
            }
        });
    }
}
