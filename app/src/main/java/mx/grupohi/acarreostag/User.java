package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.FrameLayout;

import mx.grupohi.acarreostag.DBScaSqlite;

import org.json.JSONObject;

/**
 * Creado por JFEsquivel el 27/09/2016.
 */

class User {

    static String usr;
    static String bd;
    static String proyecto;
    private Context context;

    private static SQLiteDatabase db;
    private static DBScaSqlite db_sca;

    String base;
    String  user;
     String name;
     String pass;
    Integer idUsuario;
    Integer idProyecto;
    String descripcionBaseDatos;

    User(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    User getUsuario() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM user LIMIT 1", null);
        try {
            if(c != null && c.moveToFirst()) {
                this.idUsuario = c.getInt(c.getColumnIndex("idusuario"));
                this.idProyecto = c.getInt(c.getColumnIndex("idproyecto"));
                this.name = c.getString(c.getColumnIndex("nombre"));
                this.base = c.getString(c.getColumnIndex("base_datos"));
                this.descripcionBaseDatos = c.getString(c.getColumnIndex("descripcion_database"));
                this.user = c.getString(c.getColumnIndex("usr"));
                this.pass = c.getString(c.getColumnIndex("pass"));

                return this;
            } else {
                return null;
            }
        }finally {
            c.close();
            db.close();
        }
    }
    boolean create(ContentValues values) {
        db = db_sca.getWritableDatabase();
        try{
            return db.insert("user", null, values) > -1;
        } finally {
            db.close();
        }
    }

    void deleteAll() {
        db = db_sca.getWritableDatabase();
        try{
            db.execSQL("DELETE FROM user");
        } finally {
            db.close();
        }
    }

    boolean get() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM user", null);
        try{
            return c != null && c.moveToFirst();
        } finally {
            c.close();
            db.close();
        }
    }

    String getName() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT nombre FROM user LIMIT 1", null);
        try{
            if (c != null && c.moveToFirst()) {
                name = c.getString(c.getColumnIndex("nombre"));
            }
            return name;
        } finally {
            c.close();
        }
    }

    String getPass() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT pass FROM user LIMIT 1", null);
        try{
            if (c.moveToFirst()) {
                pass = c.getString(c.getColumnIndex("pass"));
            }
            return pass;
        } finally {
            c.close();
            db.close();
        }
    }

    String getUsr() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT usr FROM user LIMIT 1", null);
        try{
            if (c.moveToFirst()) {
                pass = c.getString(c.getColumnIndex("usr"));
            }
            return pass;
        } finally {
            c.close();
            db.close();
        }
    }

    static String getProyecto(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT descripcion_database FROM user LIMIT 1", null);
        try{
            if (c.moveToFirst()) {
                proyecto = c.getString(c.getColumnIndex("descripcion_database"));
            }
            return proyecto;
        } finally {
            c.close();
            db.close();
        }
    }

    static String getIdProyecto(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT idproyecto FROM user LIMIT 1", null);
        try{
            if (c.moveToFirst()) {
                proyecto = c.getString(c.getColumnIndex("idproyecto"));
            }
            return proyecto;
        } finally {
            c.close();
            db.close();
        }
    }
    
    static String getUser(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT usr FROM user LIMIT 1", null);
        try{
            if (c.moveToFirst()) {
                usr = c.getString(c.getColumnIndex("usr"));
            }
            return usr;
        } finally {
            c.close();
            db.close();
        }
    }
    
    static String getBaseDatos(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT base_datos FROM user LIMIT 1", null);
        try{
            if (c.moveToFirst()) {
                bd = c.getString(c.getColumnIndex("base_datos"));
            }
            return bd;
        } finally {
            c.close();
            db.close();
        }
    }

    public Integer getId() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM user LIMIT 1", null);
        try {
            if(c != null && c.moveToFirst()) {
                this.idUsuario = c.getInt(0);
            }
            return this.idUsuario;
        } finally {
            c.close();
            db.close();
        }
    }

    static boolean updatePass(String pass, Context context) {
        boolean resp=false;
        ContentValues data = new ContentValues();

        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

        try{

            data.put("pass", pass);

            db.update("user", data, "", null);
            resp = true;
        } finally {
            db.close();
        }
        return resp;
    }

}
