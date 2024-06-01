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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private Context context;
    private List<CartProduct> itemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private EditText editTextCantidad, direccion, localidad, pais, titularTarjeta, numTarjeta, cadTarjeta, cvv;
    private Button buttonTramitar, buttonVolver, buttonConfirmar, buttonVolverResumen;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        context = CartActivity.this;
        sharedPreferences = context.getSharedPreferences("DoggyWorldPrefs", Context.MODE_PRIVATE);
        recyclerView = findViewById(R.id.recyclerView);
        buttonTramitar = findViewById(R.id.buttonTramitar);
        buttonVolver = findViewById(R.id.buttonVolver);

        loadCartProducts();

        buttonTramitar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.tramitar_pedido);
                direccion = findViewById(R.id.addressField);
                localidad = findViewById(R.id.localityField);
                pais = findViewById(R.id.countryField);
                titularTarjeta = findViewById(R.id.cardholderField);
                numTarjeta = findViewById(R.id.cardNumberField);
                cadTarjeta = findViewById(R.id.expiryDateField);
                cvv = findViewById(R.id.cvvField);
                buttonConfirmar = findViewById(R.id.buttonConfirmar);
                buttonVolverResumen = findViewById(R.id.buttonVolverResumen);

                buttonConfirmar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        comprobarDatos();
                    }
                });

                buttonVolverResumen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CartActivity.this, CartActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });

        buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void loadCartProducts() {
        String url = "http://10.0.2.2:8000/api/doggyWorld/cart";

        // Obtener el token de SharedPreferences
        String token = sharedPreferences.getString("token", null);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Procesar la respuesta JSON
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                // Parsear datos del producto
                                int id = jsonObject.getInt("id");
                                String nombre = jsonObject.getString("nombre");
                                int cantidad = jsonObject.getInt("cantidad");
                                double precio = jsonObject.getDouble("precio");
                                String imagen = jsonObject.getString("imagen");
                                // Crear objeto Producto y agregar a la lista
                                CartProduct producto = new CartProduct(id, nombre, cantidad, precio, imagen);
                                itemList.add(producto);
                            }
                            CartAdapter adapter = new CartAdapter(itemList, context, token);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Manejar el error de la solicitud HTTP
                        if (error.networkResponse != null) {
                            // Si hay una respuesta del servidor, obtener el código de estado y el mensaje de error
                            int statusCode = error.networkResponse.statusCode;
                            String errorMessage = new String(error.networkResponse.data);
                            // Muestra el mensaje de error en un Toast
                            Toast.makeText(context, "Error " + statusCode + ": " + errorMessage, Toast.LENGTH_LONG).show();
                        }
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
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }

    private void createOrder() {
        String url = "http://10.0.2.2:8000/api/doggyWorld/pedidos";

        // Obtener el token de SharedPreferences
        String token = sharedPreferences.getString("token", null);

        JSONObject body = new JSONObject();
        try {
            body.put("direccion", direccion.getText().toString().trim());
            body.put("localidad", localidad.getText().toString().trim());
            body.put("pais", pais.getText().toString().trim());
            body.put("titularTarjeta", titularTarjeta.getText().toString().trim());
            body.put("numTarjeta", numTarjeta.getText().toString().trim());
            body.put("cadTarjeta", cadTarjeta.getText().toString().trim());
            body.put("CVV", cvv.getText().toString().trim());
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
                        Toast.makeText(CartActivity.this, "Pedido realizado correctamente", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CartActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Maneja el error de la solicitud HTTP
                        if (error.networkResponse == null) {
                            // Si no hay respuesta del servidor, muestra un mensaje de error interno
                            Toast.makeText(CartActivity.this, "Internal Server Error", Toast.LENGTH_SHORT).show();
                        } else {
                            // Si hay una respuesta del servidor, obtener el código de estado y el mensaje de error
                            int statusCode = error.networkResponse.statusCode;
                            String errorMessage = new String(error.networkResponse.data);
                            // Muestra el mensaje de error en un Toast
                            Toast.makeText(CartActivity.this, "Error " + statusCode + ": " + errorMessage, Toast.LENGTH_LONG).show();
                        }
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

    private void comprobarDatos() {
        // Obtener los valores ingresados por el usuario
        String sDireccion = direccion.getText().toString().trim();
        String sLocalidad = localidad.getText().toString().trim();
        String sPais = pais.getText().toString().trim();
        String sTitularTarjeta = titularTarjeta.getText().toString().trim();
        String sNumTarjeta = numTarjeta.getText().toString().trim();
        String sCadTarjeta = cadTarjeta.getText().toString().trim();
        String sCvv = cvv.getText().toString().trim();

        // Verificar que todos los campos estén llenos
        if (sDireccion.isEmpty() || sLocalidad.isEmpty() || sPais.isEmpty() || sTitularTarjeta.isEmpty() || sNumTarjeta.isEmpty() || sCadTarjeta.isEmpty() || sCvv.isEmpty()) {
            Toast.makeText(context, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar el número de tarjeta tenga 16 dígitos numéricos
        if (sNumTarjeta.length() != 16 || !sNumTarjeta.matches("\\d{16}")) {
            Toast.makeText(context, "El número de tarjeta debe tener 16 dígitos numéricos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que el CVV tenga 3 dígitos numéricos
        if (sCvv.length() != 3 || !sCvv.matches("\\d{3}")) {
            Toast.makeText(context, "El CVV debe tener 3 dígitos numéricos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la fecha de caducidad del campo
        sCadTarjeta = cadTarjeta.getText().toString().trim();

        // Verificar que la fecha de caducidad siga el patrón "mm/yy" y esté dentro del rango válido
        if (!sCadTarjeta.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            Toast.makeText(context, "La fecha de caducidad debe seguir el patrón 'mm/yy' y estar dentro del rango válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el año actual
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR) % 100; // Obtiene los dos últimos dígitos del año actual

        // Separar el mes y el año de la fecha de caducidad
        String[] parts = sCadTarjeta.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);

        // Verificar si el mes está dentro del rango de 1 a 12
        if (month < 1 || month > 12) {
            Toast.makeText(context, "El mes de la fecha de caducidad debe estar entre 01 y 12.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si el año está dentro del rango del año actual en adelante
        if (year < currentYear) {
            Toast.makeText(context, "El año de la fecha de caducidad debe ser igual o posterior al año actual.", Toast.LENGTH_SHORT).show();
            return;
        }

        createOrder();
    }
}