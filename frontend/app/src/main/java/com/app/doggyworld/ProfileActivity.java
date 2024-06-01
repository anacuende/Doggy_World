package com.app.doggyworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private Button btnModDatos, btnPedidos, btnListaDeseos, btnContactoFAQ, btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        btnModDatos = findViewById(R.id.btnModDatos);
        btnPedidos = findViewById(R.id.btnPedidos);
        btnListaDeseos = findViewById(R.id.btnListaDeseos);
        btnContactoFAQ = findViewById(R.id.btnContactoFAQ);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        btnModDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ModificarDatosActivity.class);
                startActivity(intent);
            }
        });

        btnPedidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ProfileActivity.this, PedidosActivity.class);
                //startActivity(intent);
            }
        });

        btnListaDeseos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ProfileActivity.this, WhislistActivity.class);
                //startActivity(intent);
            }
        });

        btnContactoFAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ContactoFAQActivity.class);
                startActivity(intent);
            }
        });

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ProfileActivity.this, LogoutActivity.class);
                //startActivity(intent);
            }
        });
    }
}