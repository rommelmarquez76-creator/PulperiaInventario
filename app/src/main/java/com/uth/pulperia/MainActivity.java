package com.uth.pulperia;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DBHelper db;
    private ProductoAdapter adapter;
    private EditText etBuscar;
    private CheckBox cbStockBajo;
    private TextView tvResumen, tvVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DBHelper(this);

        etBuscar = findViewById(R.id.etBuscar);
        cbStockBajo = findViewById(R.id.cbStockBajo);
        tvResumen = findViewById(R.id.tvResumen);
        tvVacio = findViewById(R.id.tvVacio);
        RecyclerView rv = findViewById(R.id.rvProductos);
        Button btnVentas = findViewById(R.id.btnVentas);
        ExtendedFloatingActionButton fab = findViewById(R.id.fabAgregar);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductoAdapter(new ProductoAdapter.Listener() {
            @Override
            public void onVender(Producto p) {
                mostrarDialogoVenta(p);
            }

            @Override
            public void onEditar(Producto p) {
                Intent i = new Intent(MainActivity.this, ProductoFormActivity.class);
                i.putExtra(ProductoFormActivity.EXTRA_ID, p.getId());
                startActivity(i);
            }

            @Override
            public void onEliminar(Producto p) {
                confirmarEliminar(p);
            }
        });
        rv.setAdapter(adapter);

        fab.setOnClickListener(v ->
                startActivity(new Intent(this, ProductoFormActivity.class)));
        btnVentas.setOnClickListener(v ->
                startActivity(new Intent(this, VentasActivity.class)));

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { cargar(); }
        });
        cbStockBajo.setOnCheckedChangeListener((b, checked) -> cargar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        List<Producto> lista = db.obtenerProductos(
                etBuscar.getText().toString().trim(), cbStockBajo.isChecked());
        adapter.setLista(lista);
        tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);

        tvResumen.setText("Productos: " + db.contarProductos()
                + "  |  Stock bajo: " + db.contarStockBajo()
                + "\nInventario: " + Formato.lempiras(db.valorInventario())
                + "  |  Ventas hoy: " + Formato.lempiras(db.totalVentasHoy()));
    }

    private void confirmarEliminar(Producto p) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar producto")
                .setMessage("¿Seguro que deseas eliminar \"" + p.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    db.eliminarProducto(p.getId());
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    cargar();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoVenta(Producto p) {
        EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setHint("Cantidad (disponible: " + p.getCantidad() + ")");

        FrameLayout contenedor = new FrameLayout(this);
        int margen = (int) (24 * getResources().getDisplayMetrics().density);
        contenedor.setPadding(margen, margen / 2, margen, 0);
        contenedor.addView(et);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Vender " + p.getNombre())
                .setView(contenedor)
                .setPositiveButton("Vender", (d, w) -> {
                    int cant;
                    try {
                        cant = Integer.parseInt(et.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (db.registrarVenta(p.getId(), cant)) {
                        Toast.makeText(this, "Venta registrada: "
                                + Formato.lempiras(cant * p.getPrecio()), Toast.LENGTH_SHORT).show();
                        cargar();
                    } else {
                        Toast.makeText(this, "No hay stock suficiente", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}