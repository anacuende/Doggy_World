package com.app.doggyworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.List;
import java.util.Map;

public class WhislistAdapter extends RecyclerView.Adapter<WhislistAdapter.WhislistViewHolder>{

    private Context context;
    private List<Product> productList;
    private String token;

    public WhislistAdapter (Context context, List<Product> productList, String token) {
        this.context = context;
        this.productList = productList;
        this.token = token;
    }

    @NonNull
    @Override
    public WhislistAdapter.WhislistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_whislist, parent, false);
        return new WhislistAdapter.WhislistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WhislistAdapter.WhislistViewHolder holder, int position) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        Product product = productList.get(position);
        holder.nombre.setText(product.getNombre());
        holder.precio.setText(String.valueOf(decimalFormat.format(product.getPrecio())+" €"));
        Glide.with(context).load(product.getImagen()).into(holder.imagen);

        holder.addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProductToCart(product.getId());
            }
        });

        holder.removeFromWishlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteWhislistProduct(product.getId(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class WhislistViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, precio;
        ImageView imagen;
        ImageButton addToCartButton, removeFromWishlistButton;

        public WhislistViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.productNombre);
            precio = itemView.findViewById(R.id.productPrecio);
            imagen = itemView.findViewById(R.id.productImagen);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
            removeFromWishlistButton = itemView.findViewById(R.id.removeFromWishlistButton);
        }
    }

    private void addProductToCart(int id) {
        String url = "http://10.0.2.2:8000/api/doggyWorld/cart";

        JSONObject body = new JSONObject();
        try {
            body.put("producto_id", id);
            body.put("cantidad", 1);
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
                        Toast.makeText(context, "Producto agregado al carrito de compras correctamente", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Maneja el error de la solicitud HTTP
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

        // Agrega la solicitud a la cola de solicitudes de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }

    private void deleteWhislistProduct(int id, int position) {
        String url = "http://10.0.2.2:8000/api/doggyWorld/wishlist?productId=" + id;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(context, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                        productList.remove(position);
                        notifyItemRemoved(position);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Maneja el error de la solicitud HTTP
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
        ){
            @Override
            public Map<String, String> getHeaders() {
                // Adjuntar el token de usuario a los encabezados de la solicitud
                Map<String, String> headers = new HashMap<>();
                headers.put("token", token);
                return headers;
            }
        };

        // Agrega la solicitud a la cola de solicitudes de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }
}
