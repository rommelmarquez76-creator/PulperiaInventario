package com.uth.pulperia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pulperia.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE productos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "categoria TEXT, " +
                "precio REAL NOT NULL, " +
                "cantidad INTEGER NOT NULL, " +
                "stock_minimo INTEGER NOT NULL DEFAULT 5)");

        db.execSQL("CREATE TABLE ventas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "producto_id INTEGER, " +
                "producto_nombre TEXT, " +
                "cantidad INTEGER NOT NULL, " +
                "total REAL NOT NULL, " +
                "fecha TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ventas");
        db.execSQL("DROP TABLE IF EXISTS productos");
        onCreate(db);
    }

    // ---------------- PRODUCTOS ----------------

    private ContentValues aValores(Producto p) {
        ContentValues v = new ContentValues();
        v.put("nombre", p.getNombre());
        v.put("categoria", p.getCategoria());
        v.put("precio", p.getPrecio());
        v.put("cantidad", p.getCantidad());
        v.put("stock_minimo", p.getStockMinimo());
        return v;
    }

    public long insertarProducto(Producto p) {
        return getWritableDatabase().insert("productos", null, aValores(p));
    }

    public int actualizarProducto(Producto p) {
        return getWritableDatabase().update("productos", aValores(p),
                "id = ?", new String[]{String.valueOf(p.getId())});
    }

    public int eliminarProducto(int id) {
        return getWritableDatabase().delete("productos",
                "id = ?", new String[]{String.valueOf(id)});
    }

    private Producto desdeCursor(Cursor c) {
        return new Producto(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("nombre")),
                c.getString(c.getColumnIndexOrThrow("categoria")),
                c.getDouble(c.getColumnIndexOrThrow("precio")),
                c.getInt(c.getColumnIndexOrThrow("cantidad")),
                c.getInt(c.getColumnIndexOrThrow("stock_minimo")));
    }

    public Producto obtenerProducto(int id) {
        Cursor c = getReadableDatabase().query("productos", null,
                "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        Producto p = null;
        if (c.moveToFirst()) {
            p = desdeCursor(c);
        }
        c.close();
        return p;
    }

    public List<Producto> obtenerProductos(String filtro, boolean soloStockBajo) {
        List<Producto> lista = new ArrayList<>();
        StringBuilder where = new StringBuilder("1=1");
        List<String> args = new ArrayList<>();

        if (filtro != null && !filtro.isEmpty()) {
            where.append(" AND (nombre LIKE ? OR categoria LIKE ?)");
            args.add("%" + filtro + "%");
            args.add("%" + filtro + "%");
        }
        if (soloStockBajo) {
            where.append(" AND cantidad <= stock_minimo");
        }

        Cursor c = getReadableDatabase().query("productos", null,
                where.toString(), args.toArray(new String[0]),
                null, null, "nombre COLLATE NOCASE ASC");
        while (c.moveToNext()) {
            lista.add(desdeCursor(c));
        }
        c.close();
        return lista;
    }

    public int contarProductos() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM productos", null);
        int n = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return n;
    }

    public int contarStockBajo() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM productos WHERE cantidad <= stock_minimo", null);
        int n = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return n;
    }

    public double valorInventario() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT SUM(precio * cantidad) FROM productos", null);
        double v = c.moveToFirst() ? c.getDouble(0) : 0;
        c.close();
        return v;
    }

    // ---------------- VENTAS ----------------

    /** Registra la venta y descuenta el stock. Devuelve false si no hay stock suficiente. */
    public boolean registrarVenta(int productoId, int cantidadVendida) {
        if (cantidadVendida <= 0) return false;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor c = db.rawQuery("SELECT nombre, precio, cantidad FROM productos WHERE id = ?",
                    new String[]{String.valueOf(productoId)});
            if (!c.moveToFirst()) {
                c.close();
                return false;
            }
            String nombre = c.getString(0);
            double precio = c.getDouble(1);
            int stock = c.getInt(2);
            c.close();

            if (cantidadVendida > stock) return false;

            db.execSQL("UPDATE productos SET cantidad = cantidad - ? WHERE id = ?",
                    new Object[]{cantidadVendida, productoId});

            ContentValues v = new ContentValues();
            v.put("producto_id", productoId);
            v.put("producto_nombre", nombre);
            v.put("cantidad", cantidadVendida);
            v.put("total", precio * cantidadVendida);
            v.put("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
            db.insert("ventas", null, v);

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public List<String> obtenerVentas() {
        List<String> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT producto_nombre, cantidad, total, fecha FROM ventas ORDER BY id DESC", null);
        while (c.moveToNext()) {
            lista.add(c.getString(0) + "  x" + c.getInt(1) + "  -  " +
                    Formato.lempiras(c.getDouble(2)) + "\n" + c.getString(3));
        }
        c.close();
        return lista;
    }

    public double totalVentasHoy() {
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT SUM(total) FROM ventas WHERE fecha LIKE ?", new String[]{hoy + "%"});
        double t = c.moveToFirst() ? c.getDouble(0) : 0;
        c.close();
        return t;
    }
}
