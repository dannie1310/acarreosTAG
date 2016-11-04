package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Contacts;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.Objects;

/**
 * Creado por JFEsquivel on 28/09/2016.
 */

class TagModel {
    private Context context;
    private ContentValues data;

    private static SQLiteDatabase db;
    private DBScaSqlite db_sca;

    TagModel(Context context) {
        this.context = context;
        this.data = new ContentValues();
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
        this.data.clear();
    }

    boolean registrarTags(JSONObject data) throws Exception {
        this.data.clear();
        this.data.put("uid", data.getString("uid"));
        this.data.put("idcamion", data.getString("idcamion"));
        this.data.put("idproyecto", data.getString("idproyecto"));

        db = db_sca.getWritableDatabase();
        try{
            return db.insert("tags", null, this.data) > -1;
        } finally {
            db.close();
        }
    }

    void deleteAll() {
        db = db_sca.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM tags");
            db.execSQL("DELETE FROM tags_disponibles");
        } finally {
            db.close();
        }
    }

    boolean registrarTagsDisponibles(JSONObject data) throws Exception {
        this.data.clear();
        this.data.put("uid", data.getString("uid"));
        this.data.put("idtag", data.getString("id"));
        this.data.put("idcamion", data.getString("idcamion") != "null" ? data.getString("idcamion") : null);

        db = db_sca.getWritableDatabase();
        try{
            return db.insert("tags_disponibles", null, this.data) > -1;
        } finally {
            db.close();
        }
    }

    static boolean areSynchronized(Context context) {
        Boolean result = true;
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT idcamion FROM tags_disponibles", null);
        try{
            if (c != null) {
                while (c.moveToNext()) {

                    if (c.getString(c.getColumnIndex("idcamion")) != null) {
                        result = false;
                    }
                }
            }
            return result;
        } finally {
            c.close();
            db.close();
        }
    }

    boolean exists(String UID) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM (SELECT uid FROM tags UNION SELECT uid FROM tags_disponibles) as total WHERE uid = '" + UID + "'", null);
        try{
            return c != null && c.moveToFirst();
        } finally {
            c.close();
            db.close();
        }
    }

    static JSONObject getJSON(Context context) {
        JSONObject JSON = new JSONObject();
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM  tags_disponibles WHERE idcamion  IS NOT NULL", null);
        try{
            if (c != null && c.moveToFirst()) {
                int i = 0;
                do {
                    JSONObject json = new JSONObject();

                    json.put("uid", c.getString(c.getColumnIndex("uid")));
                    json.put("idcamion", c.getString(c.getColumnIndex("idcamion")));
                    json.put("idtag", c.getString(c.getColumnIndex("idtag")));
                    json.put("idproyecto_global", User.getIdProyecto(context));

                    JSON.put(i + "", json);
                    i++;
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
        return JSON;
    }

    void update(String UID, String idcamion) {
        this.data.clear();
        this.data.putNull("idcamion");

        db = db_sca.getWritableDatabase();

        try{
            db.update("tags_disponibles", this.data, "idcamion = '"  + idcamion + "'", null);
            this.data.clear();
            this.data.put("idcamion", idcamion);
            db.update("tags_disponibles", this.data, "uid = '"+ UID +"'", null);
        } finally {
            db.close();
        }
    }

    boolean tagDisponible (String UID) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tags_disponibles WHERE uid = '" + UID + "'", null);
        try{
            return c != null && c.moveToFirst();
        } finally {
            c.close();
            db.close();
        }
    }

    static void sync(Context context) {
        ContentValues data = new ContentValues();
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT uid, idcamion FROM tags_disponibles WHERE idcamion IS NOT NULL", null);
        try {
            if(c != null && c.moveToFirst()) {
                do {
                    data.clear();
                    data.put("uid", c.getString(c.getColumnIndex("uid")) );
                    data.put("idcamion", c.getString(c.getColumnIndex("idcamion")));
                    data.put("idproyecto", User.getProyecto(context));

                    db.insert("tags", null, data);
                    db.execSQL("DELETE FROM tags_disponibles WHERE uid = '" + c.getString(c.getColumnIndex("uid")) + "'");
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
    }
}
