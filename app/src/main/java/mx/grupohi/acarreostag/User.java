package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import mx.grupohi.acarreostag.DBScaSqlite;

import org.json.JSONObject;

/**
 * Created by JFEsquivel on 27/09/2016.
 */

public class User {

    private static String usr;
    private static String bd;
    private static String proyecto;
    private Context context;

    private static SQLiteDatabase db;
    private static DBScaSqlite db_sca;

    private String name;
    private String pass;

    public User(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    public boolean create(ContentValues values) {
        return db.insert("user", null, values) > -1;
    }

    public void deleteAll() {
        db.execSQL("DELETE FROM user");
    }

    public boolean get() {
        Cursor c = db.rawQuery("SELECT * FROM user", null);
        return c != null && c.moveToFirst();
    }

    public String getName() {
        Cursor c = db.rawQuery("SELECT nombre FROM user LIMIT 1", null);
        if (c.moveToFirst()) {
            name = c.getString(c.getColumnIndex("nombre"));
        }
        return name;
    }

    public String getPass() {
        Cursor c = db.rawQuery("SELECT pass FROM user LIMIT 1", null);
        if (c.moveToFirst()) {
            pass = c.getString(c.getColumnIndex("pass"));
        }
        return pass;
    }

    public static String getProyecto() {
        Cursor c = db.rawQuery("SELECT descripcion_database FROM user LIMIT 1", null);
        if (c.moveToFirst()) {
            proyecto = c.getString(c.getColumnIndex("descripcion_database"));
        }
        return proyecto;
    }

    public String getIdProyecto() {
        Cursor c = db.rawQuery("SELECT idproyecto FROM user LIMIT 1", null);
        if (c.moveToFirst()) {
            proyecto = c.getString(c.getColumnIndex("idproyecto"));
        }
        return proyecto;
    }
    
    public static String getUser() {
        Cursor c = db.rawQuery("SELECT usr FROM user LIMIT 1", null);
        if (c.moveToFirst()) {
            usr = c.getString(c.getColumnIndex("usr"));
        }
        return usr;
    }
    
    public static String getBaseDatos() {
        Cursor c = db.rawQuery("SELECT base_datos FROM user LIMIT 1", null);
        if (c.moveToFirst()) {
            bd = c.getString(c.getColumnIndex("base_datos"));
        }
        return bd;
    }
}
