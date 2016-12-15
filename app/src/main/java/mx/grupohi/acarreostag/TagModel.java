package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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

    public String UID;

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
        this.data.put("estatus",0);
        System.out.println("estatus: "+this.data);
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

    static boolean exists(String UID, Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
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

    static boolean update(String UID, String idcamion, Context context, Boolean sync) {
        boolean resp=false;
        ContentValues data = new ContentValues();

        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

        if(!sync) {
            data.putNull("idcamion");
            try{
                System.out.println("DISPONIBLE: "+camionDisponible(idcamion,context));
                if(!camionDisponible(idcamion,context)) {
                    db.update("tags_disponibles", data, "idcamion = '" + idcamion + "'", null);
                    data.clear();
                    data.put("idcamion", idcamion);
                    db.update("tags_disponibles", data, "uid = '" + UID + "'", null);
                    System.out.println("1: " + idcamion + "tag: " + UID);
                    resp = true;
                }else{
                    resp = false;
                }
            } finally {
                db.close();
            }
        } else {
            try{
                db.execSQL("DELETE FROM tags WHERE idcamion = '" + idcamion + "'");
                data.clear();
                data.put("idcamion", idcamion);
                data.put("estatus", "1");

                db.update("tags_disponibles", data, "uid = '"+ UID +"'", null);
                resp = true;
            } finally {
                db.close();
            }
        }
        return resp;
    }

    static boolean tagDisponible (String UID, Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tags_disponibles WHERE uid = '" + UID + "' and estatus = 0", null);
        try{
            return c != null && c.moveToFirst();
        } finally {
            c.close();
            db.close();
        }
    }

    static boolean camionDisponible (String idcamion, Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tags_disponibles WHERE idcamion = '" + idcamion + "' and estatus = 1", null);
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

    TagModel find(String UID) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tags WHERE uid = '" + UID + "'", null);
        try{
            if(c != null && c.moveToFirst()) {
                this.UID = c.getString(c.getColumnIndex("uid"));
            }
            return this;
        } finally {
            c.close();
            db.close();
        }
    }

    static String findCamion (String UID, Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT tags.uid, tags.idcamion, camiones.economico, camiones.placas FROM tags INNER JOIN camiones ON camiones.idcamion = tags.idcamion  WHERE tags.uid = '" + UID + "'", null);//CHECAR

        String resp = null;
        try{
            if(c != null && c.moveToFirst()) {
                resp = " "+c.getString(2) +"["+c.getString(3)+"]";
            }
            return resp;
        } finally {
            c.close();
            db.close();
        }
    }
    static String findDisponibleCamion (String UID, Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT tags_disponibles.uid, tags_disponibles.idcamion, camiones.economico, camiones.placas FROM tags_disponibles INNER JOIN camiones ON camiones.idcamion = tags_disponibles.idcamion  WHERE tags_disponibles.uid = '" + UID + "' and estatus = 1", null);//CHECAR

        String resp = null;
        try{
            if(c != null && c.moveToFirst()) {
                resp = " "+c.getString(2) +"["+c.getString(3)+"]";
            }
            System.out.println("RESP: "+ resp + " : "+UID);
            return resp;
        } finally {
            c.close();
            db.close();
        }
    }
}
