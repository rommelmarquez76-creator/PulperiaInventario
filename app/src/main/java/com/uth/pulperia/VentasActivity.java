package com.uth.pulperia;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class VentasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ventas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ventasRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DBHelper db = new DBHelper(this);
        TextView tvTotalHoy = findViewById(R.id.tvTotalHoy);
        TextView tvSinVentas = findViewById(R.id.tvSinVentas);
        ListView lv = findViewById(R.id.lvVentas);
        Button btnVolver = findViewById(R.id.btnVolver);

        tvTotalHoy.setText("Ventas de hoy: " + Formato.lempiras(db.totalVentasHoy()));

        List<String> ventas = db.obtenerVentas();
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ventas));
        tvSinVentas.setVisibility(ventas.isEmpty() ? TextView.VISIBLE : TextView.GONE);

        btnVolver.setOnClickListener(v -> finish());
    }
}