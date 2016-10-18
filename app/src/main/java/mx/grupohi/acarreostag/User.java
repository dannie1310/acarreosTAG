package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import mx.grupohi.acarreostag.DBScaSqlite;

import org.json.JSONObject;

/**
 * Creado por JFEsquivel el 27/09/2016.
 */

class User {

    private static String usr;
    private static String bd;
    private static String proyecto;
    private Context context;

    private static SQLiteDatabase db;
    private static DBScaSqlite db_sca;

    private String name;
    private String pass;

    User(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    boolean create(ContentValues values) {
        return db.insert("user", null, values) > -1;
    }

    void deleteAll() {
        db.execSQL("DELETE FROM user");
    }

    boolean get() {
        Boolean result;
        Cursor c = db.rawQuery("SELECT * FROM user", null);
        result = c != null && c.moveToFirst();
        assert c != null;
        c.close();
        return result;
    }

    String getName() {
        Cursor c = db.rawQuery("SELECT nombre FROM user LIMIT 1", null);
        if (c != null && c.moveToFirst()) {
            name = c.getString(c.getColumnIndex("nombre"));
        }
        assert c != null;
        c.close();
        return name;
    }

    String getPass() {
        Cursor c = db.rawQuery("SELECT pass FROM user LIMIT 1", null);
        if (c.moveToFirst()) {
            pass = c.getString(c.getColumnIndex("pass"));
        }
        c.close();
        return pass;
    }

    static String getProyecto() {
        Cursor c = db.rawQuery("SELECT descripcion_database FROM user LIMIT 1", null);
        if (c.moveToFirst()) {
            proyecto = c.getString(c.getColumnIndex("descripcion_database"));
        }
        c.close();
        return proyecto;
    }

    static String getIdProyecto() {
        Cursor c = db.rawQuery("SELECT idproyecto FROM user LIMIT 1", null);
        if (c.moveToFirst()) {
            proyecto = c.getString(c.getColumnIndex("idproyecto"));
        }
        c.close();
        return proyecto;
    }
    
    static String getUser() {
        Cursor c = db.rawQuery("SELECT usr FROM user LIMIT 1", null);
        if (c.moveToFirst()) {
            usr = c.getString(c.getColumnIndex("usr"));
        }
        c.close();
        return usr;
    }
    
    static String getBaseDatos() {
        Cursor c = db.rawQuery("SELECT base_datos FROM user LIMIT 1", null);
        if (c.moveToFirst()) {
            bd = c.getString(c.getColumnIndex("base_datos"));
        }
        c.close();
        return bd;
    }
}
