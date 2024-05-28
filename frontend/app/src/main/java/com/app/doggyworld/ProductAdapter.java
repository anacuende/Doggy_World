package com.app.doggyworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import android.content.Intent;
import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        Product product = productList.get(position);
        holder.nombre.setText(product.getNombre());
        holder.precio.setText(String.valueOf(decimalFormat.format(product.getPrecio())+" €"));
        Glide.with(context).load(product.getImagen()).into(holder.imagen);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productId", product.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, precio;
        ImageView imagen;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.productNombre);
            precio = itemView.findViewById(R.id.productPrecio);
            imagen = itemView.findViewById(R.id.productImagen);
        }
    }
}
