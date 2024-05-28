package com.app.doggyworld;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriaActivity {

    private Context context;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private SharedPreferences sharedPreferences;

    public CategoriaActivity(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        sharedPreferences = context.getSharedPreferences("DoggyWorldPrefs", Context.MODE_PRIVATE);
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(context, productList);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.recyclerView.setAdapter(productAdapter);
    }

    public void loadProducts(String queryParam) {
        String url = "http://10.0.2.2:8000/api/doggyWorld/products?" + queryParam;

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
                            productList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                // Parsear datos del producto
                                int id = jsonObject.getInt("id");
                                String nombre = jsonObject.getString("nombre");
                                String descripcion = jsonObject.getString("descripcion");
                                double precio = jsonObject.getDouble("precio");
                                String imagen = jsonObject.getString("imagen");
                                // Crear objeto Producto y agregar a la lista
                                Product producto = new Product(id, nombre, descripcion, precio, imagen);
                                productList.add(producto);
                            }
                            // Actualizar la interfaz con la lista de productos
                            productAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Manejar el error de la solicitud HTTP
                        if (error.networkResponse == null) {
                            // Si no hay respuesta del servidor, muestra un mensaje de error interno
                            Toast.makeText(context, "Internal Server Error", Toast.LENGTH_SHORT).show();
                        } else {
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
}
