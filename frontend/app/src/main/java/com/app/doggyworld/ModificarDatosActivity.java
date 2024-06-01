package com.app.doggyworld;

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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ModificarDatosActivity extends AppCompatActivity {

    private EditText textNombre, textNombreUsuario, textEmail, textContrasena, textRContrasena;
    private Button btnGuardarCambios;
    private String token;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_datos);

        textNombre = findViewById(R.id.textNombre);
        textNombreUsuario = findViewById(R.id.textNombreUsuario);
        textEmail = findViewById(R.id.textEmail);
        textContrasena = findViewById(R.id.textContrasena);
        textRContrasena = findViewById(R.id.textRContrasena);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("DoggyWorldPrefs", MODE_PRIVATE);

        // Obtener el token de SharedPreferences
        token = sharedPreferences.getString("token", null);

        cargarDatosUsuario();

        btnGuardarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textEmail.getText().toString().trim().contains("@")) {
                    if (textContrasena.getText().toString().trim().equals(textRContrasena.getText().toString().trim())) {
                        actualizarDatosUsuario();
                    }
                    else {
                        Toast.makeText(ModificarDatosActivity.this, "Contrasñas no coinciden", Toast.LENGTH_SHORT).show();
                    }
                }
                else  {
                    Toast.makeText(ModificarDatosActivity.this, "Email no valido", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cargarDatosUsuario() {
        String url = "http://10.0.2.2:8000/api/doggyWorld/user/register";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            textNombre.setText(response.getString("name"));
                            textNombreUsuario.setText(response.getString("username"));
                            textEmail.setText(response.getString("email"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    // Manejar el error de la solicitud HTTP
                    if (error.networkResponse == null) {
                        // Si no hay respuesta del servidor, muestra un mensaje de error interno
                        Toast.makeText(ModificarDatosActivity.this, "Error interno del servidor", Toast.LENGTH_SHORT).show();
                    } else {
                        // Si hay una respuesta del servidor, obtener el código de estado y el mensaje de error
                        int statusCode = error.networkResponse.statusCode;
                        String errorMessage = new String(error.networkResponse.data);
                        // Muestra el mensaje de error en un Toast
                        Toast.makeText(ModificarDatosActivity.this, "Error " + statusCode + ": " + errorMessage, Toast.LENGTH_LONG).show();
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

        // Agregar la solicitud a la cola de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void actualizarDatosUsuario() {
        String url = "http://10.0.2.2:8000/api/doggyWorld/user";

        // Obtener el token de SharedPreferences
        String token = sharedPreferences.getString("token", null);

        JSONObject body = new JSONObject();
        try {
            body.put("name", textNombre.getText().toString().trim());
            body.put("username", textNombreUsuario.getText().toString().trim());
            body.put("email", textEmail.getText().toString().trim());
            body.put("confirmPassword", textRContrasena.getText().toString().trim());
            if (!textContrasena.getText().toString().trim().isEmpty() ) {
                body.put("password", textContrasena.getText().toString().trim());
            }
            if (!textContrasena.getText().toString().trim().isEmpty() ) {
                body.put("confirmPassword", textContrasena.getText().toString().trim());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(ModificarDatosActivity.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Manejar el error de la solicitud HTTP
                    if (error.networkResponse == null) {
                        // Si no hay respuesta del servidor, muestra un mensaje de error interno
                        Toast.makeText(ModificarDatosActivity.this, "Error interno del servidor", Toast.LENGTH_SHORT).show();
                    } else {
                        // Si hay una respuesta del servidor, obtener el código de estado y el mensaje de error
                        int statusCode = error.networkResponse.statusCode;
                        String errorMessage = new String(error.networkResponse.data);
                        // Muestra el mensaje de error en un Toast
                        Toast.makeText(ModificarDatosActivity.this, "Error " + statusCode + ": " + errorMessage, Toast.LENGTH_LONG).show();
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
