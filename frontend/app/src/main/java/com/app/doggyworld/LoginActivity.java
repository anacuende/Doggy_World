package com.app.doggyworld;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEmailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEmailEditText = findViewById(R.id.usernameEmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        sharedPreferences = getSharedPreferences("DoggyWorldPrefs", Context.MODE_PRIVATE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameEmail = usernameEmailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (usernameEmail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Rellene todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    iniciarSesion(usernameEmail, password);
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegación a la actividad de registro
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void iniciarSesion(String usernameEmail, String password) {
        String url = "http://10.0.2.2:8000/api/doggyWorld/session";
        JSONObject body = new JSONObject();
        try {
            body.put("user", usernameEmail);
            body.put("password", password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Obtener el mensaje de respuesta del servidor
                            String message = response.getString("message");
                            // Muestra el mensaje en un Toast
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                            String token = response.getString("token");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("token", token);
                            editor.apply();

                            // Navega a MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            // Si hay un error al procesar la respuesta, muestra un mensaje de error genérico
                            Toast.makeText(LoginActivity.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Maneja el error de la solicitud HTTP
                        if (error.networkResponse == null) {
                            // Si no hay respuesta del servidor, muestra un mensaje de error interno
                            Toast.makeText(LoginActivity.this, "Internal Server Error", Toast.LENGTH_SHORT).show();
                        } else {
                            // Si hay una respuesta del servidor, obtener el código de estado y el mensaje de error
                            int statusCode = error.networkResponse.statusCode;
                            String errorMessage = new String(error.networkResponse.data);
                            // Muestra el mensaje de error en un Toast
                            Toast.makeText(LoginActivity.this, "Error " + statusCode + ": " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        // Agrega la solicitud a la cola de solicitudes de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
}
