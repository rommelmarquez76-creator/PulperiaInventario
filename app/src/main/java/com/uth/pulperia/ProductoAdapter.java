package com.uth.pulperia;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.VH> {

    public interface Listener {
        void onVender(Producto p);
        void onEditar(Producto p);
        void onEliminar(Producto p);
    }

    private List<Producto> lista = new ArrayList<>();
    private final Listener listener;

    public ProductoAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setLista(List<Producto> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Producto p = lista.get(position);
        h.tvNombre.setText(p.getNombre());
        h.tvPrecio.setText(Formato.lempiras(p.getPrecio()));
        String cat = (p.getCategoria() == null || p.getCategoria().isEmpty())
                ? "Sin categoría" : p.getCategoria();
        h.tvCategoria.setText(cat);

        if (p.isStockBajo()) {
            h.tvStock.setText("Stock: " + p.getCantidad() + "  ⚠ STOCK BAJO (mín. " + p.getStockMinimo() + ")");
            h.tvStock.setTextColor(Color.parseColor("#C62828"));
        } else {
            h.tvStock.setText("Stock: " + p.getCantidad());
            h.tvStock.setTextColor(h.colorNormal);
        }

        h.btnVender.setOnClickListener(v -> listener.onVender(p));
        h.btnEditar.setOnClickListener(v -> listener.onEditar(p));
        h.btnEliminar.setOnClickListener(v -> listener.onEliminar(p));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPrecio, tvCategoria, tvStock;
        Button btnVender, btnEditar, btnEliminar;
        int colorNormal;

        VH(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvStock = itemView.findViewById(R.id.tvStock);
            btnVender = itemView.findViewById(R.id.btnVender);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            colorNormal = tvStock.getCurrentTextColor();
        }
    }
}