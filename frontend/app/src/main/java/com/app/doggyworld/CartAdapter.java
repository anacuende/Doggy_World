package com.app.doggyworld;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartProduct> itemList;
    private Context context;
    private String token = "";

    public CartAdapter(List<CartProduct> itemList, Context context, String token) {
        this.itemList = itemList;
        this.context = context;
        this.token = token;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        CartProduct item = itemList.get(position);
        holder.itemName.setText(item.getNombre());
        holder.itemPrice.setText(String.valueOf(decimalFormat.format(item.getPrecio())+" €"));
        holder.itemQuantity.setText(String.valueOf(item.getCantidad()));
        Glide.with(context).load(item.getImagen()).into(holder.itemImage);

        holder.itemRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove item logic
                deleteCartProduct(itemList.get(position).getId());
                itemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, itemList.size());
            }
        });
        modificarCantidad(holder, itemList.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
    private void deleteCartProduct(int id) {
        String url = "http://10.0.2.2:8000/api/doggyWorld/cart?productId=" + id;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(context, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
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

    private void modificarCantidad(ViewHolder holder, int id) {
        holder.itemQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    String url = "http://10.0.2.2:8000/api/doggyWorld/cart";
                    JSONObject body = new JSONObject();
                    try {
                        body.put("producto_id", id);
                        body.put("cantidad", Integer.parseInt(s.toString()));
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
                                    Toast.makeText(context, "Cantidad actualizada correctamente", Toast.LENGTH_SHORT).show();
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
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView itemImage;
        public TextView itemName;
        public TextView itemPrice;
        public EditText itemQuantity;
        public Button itemRemoveButton;

        public ViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemName = itemView.findViewById(R.id.item_name);
            itemPrice = itemView.findViewById(R.id.item_price);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
            itemRemoveButton = itemView.findViewById(R.id.item_remove_button);
        }
    }
}