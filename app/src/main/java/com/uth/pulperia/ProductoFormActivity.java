package com.uth.pulperia;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProductoFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "producto_id";

    private DBHelper db;
    private int productoId = -1;
    private EditText etNombre, etCategoria, etPrecio, etCantidad, etStockMinimo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_producto_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.form), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DBHelper(this);

        TextView tvTitulo = findViewById(R.id.tvTituloForm);
        etNombre = findViewById(R.id.etNombre);
        etCategoria = findViewById(R.id.etCategoria);
        etPrecio = findViewById(R.id.etPrecio);
        etCantidad = findViewById(R.id.etCantidad);
        etStockMinimo = findViewById(R.id.etStockMinimo);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnCancelar = findViewById(R.id.btnCancelar);

        productoId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (productoId != -1) {
            tvTitulo.setText("Editar producto");
            Producto p = db.obtenerProducto(productoId);
            if (p != null) {
                etNombre.setText(p.getNombre());
                etCategoria.setText(p.getCategoria());
                etPrecio.setText(String.valueOf(p.getPrecio()));
                etCantidad.setText(String.valueOf(p.getCantidad()));
                etStockMinimo.setText(String.valueOf(p.getStockMinimo()));
            }
        } else {
            tvTitulo.setText("Nuevo producto");
            etStockMinimo.setText("5");
        }

        btnGuardar.setOnClickListener(v -> guardar());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        String precioTxt = etPrecio.getText().toString().trim().replace(',', '.');
        String cantidadTxt = etCantidad.getText().toString().trim();
        String minimoTxt = etStockMinimo.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("Ingresa el nombre");
            return;
        }
        if (precioTxt.isEmpty()) {
            etPrecio.setError("Ingresa el precio");
            return;
        }
        if (cantidadTxt.isEmpty()) {
            etCantidad.setError("Ingresa la cantidad");
            return;
        }

        double precio;
        int cantidad;
        int minimo;
        try {
            precio = Double.parseDouble(precioTxt);
            cantidad = Integer.parseInt(cantidadTxt);
            minimo = minimoTxt.isEmpty() ? 5 : Integer.parseInt(minimoTxt);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Revisa los números ingresados", Toast.LENGTH_SHORT).show();
            return;
        }
        if (precio < 0 || cantidad < 0 || minimo < 0) {
            Toast.makeText(this, "No se permiten valores negativos", Toast.LENGTH_SHORT).show();
            return;
        }

        Producto p = new Producto(productoId, nombre, categoria, precio, cantidad, minimo);
        if (productoId == -1) {
            db.insertarProducto(p);
            Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show();
        } else {
            db.actualizarProducto(p);
            Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
