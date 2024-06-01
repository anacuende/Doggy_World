package com.app.doggyworld;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private Button btnModDatos, btnPedidos, btnListaDeseos, btnContactoFAQ, btnCerrarSesion;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences("DoggyWorldPrefs", MODE_PRIVATE);

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
                Intent intent = new Intent(ProfileActivity.this, PedidosActivity.class);
                startActivity(intent);
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
                popUpCerrarSesion();
            }
        });
    }

    private void popUpCerrarSesion() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_logout);
        dialog.setCancelable(true);

        // Set the dialog window attributes
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnConfirmLogout = dialog.findViewById(R.id.btnConfirmLogout);
        Button btnCancelLogout = dialog.findViewById(R.id.btnCancelLogout);

        btnConfirmLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
                dialog.dismiss();
            }
        });

        btnCancelLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void cerrarSesion() {
        String url = "http://10.0.2.2:8000/api/doggyWorld/session";

        // Obtener el token de SharedPreferences
        String token = sharedPreferences.getString("token", null);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(ProfileActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                error -> {
                    // Maneja el error de la solicitud HTTP
                    if (error.networkResponse == null) {
                        // Si no hay respuesta del servidor, muestra un mensaje de error interno
                        Toast.makeText(ProfileActivity.this, "Error interno del servidor", Toast.LENGTH_SHORT).show();
                    } else {
                        // Si hay una respuesta del servidor, obtener el código de estado y el mensaje de error
                        int statusCode = error.networkResponse.statusCode;
                        String errorMessage = new String(error.networkResponse.data);
                        // Muestra el mensaje de error en un Toast
                        Toast.makeText(ProfileActivity.this, "Error " + statusCode + ": " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                // Adjuntar el token de usuario a los encabezados de la solicitud
                Map<String, String> headers = new HashMap<>();
                headers.put("token", token);
                return headers;
            }
        };

        // Agrega la solicitud a la cola de solicitudes de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
}