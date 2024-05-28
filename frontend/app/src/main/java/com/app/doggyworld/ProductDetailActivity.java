package com.app.doggyworld;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private Context context;
    private ImageView productImage;
    private TextView productName, productPrice, productDescription;
    private ImageButton addToCartButton, addToWishlistButton, removeFromWishlistButton;
    private int productId;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        context = ProductDetailActivity.this;
        productImage = findViewById(R.id.productImage);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        addToCartButton = findViewById(R.id.addToCartButton);
        addToWishlistButton = findViewById(R.id.addToWishlistButton);
        removeFromWishlistButton = findViewById(R.id.removeFromWishlistButton);
        sharedPreferences = context.getSharedPreferences("DoggyWorldPrefs", Context.MODE_PRIVATE);

        productId = getIntent().getIntExtra("productId", -1);
        if (productId != -1) {
            loadProductDetails(productId);
        }

        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        addToWishlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        removeFromWishlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void loadProductDetails(int productId) {
        String url = "http://10.0.2.2:8000/api/doggyWorld/products/" + productId;

        // Obtener el token de SharedPreferences
        String token = sharedPreferences.getString("token", null);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String nombre = response.getString("nombre");
                            String descripcion = response.getString("descripcion");
                            double precio = response.getDouble("precio");
                            String imagen = response.getString("imagen");

                            DecimalFormat decimalFormat = new DecimalFormat("0.00");
                            String precioFormateado = decimalFormat.format(precio) + " €";

                            productName.setText(nombre);
                            productDescription.setText(descripcion);
                            productPrice.setText(precioFormateado);
                            Glide.with(ProductDetailActivity.this).load(imagen).into(productImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse == null) {
                            Toast.makeText(ProductDetailActivity.this, "Internal Server Error", Toast.LENGTH_SHORT).show();
                        } else {
                            int statusCode = error.networkResponse.statusCode;
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(ProductDetailActivity.this, "Error " + statusCode + ": " + errorMessage, Toast.LENGTH_LONG).show();
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

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
}
