package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Queue;

/**
 * Creado por JFEsquivel el 28/09/2016.
 */

class Camion {

    private SQLiteDatabase db;
    private Context context;
    private ContentValues data;
    public TagModel tag;

    private DBScaSqlite db_sca;

    public Integer idCamion;
    public String placas;
    public String marca;
    public String modelo;
    public Double ancho;
    public Double largo;
    public Double alto;
    public String economico;

    Camion(Context context) {
        this.context = context;
        data = new ContentValues();
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
        this.tag = new TagModel(context);
    }

    static Cursor get(String idCamion, Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        return db.rawQuery("SELECT * FROM camiones WHERE idcamion = '" + idCamion + "'", null);
    }

    boolean create(JSONObject data) throws Exception {

        this.data.put("idCamion", data.getString("idcamion"));
        this.data.put("placas", data.getString("placas"));
        this.data.put("marca", data.getString("marca"));
        this.data.put("modelo", data.getString("modelo"));
        this.data.put("ancho", data.getString("ancho"));
        this.data.put("largo", data.getString("largo"));
        this.data.put("alto", data.getString("alto"));
        this.data.put("economico", data.getString("economico"));
        this.data.put("numero_viajes", data.getString("numero_viajes"));

        db = db_sca.getWritableDatabase();
        try{
            return db.insert("camiones", null, this.data) > -1;
        } finally {
            db.close();
        }
    }

    void deleteAll() {
        db = db_sca.getWritableDatabase();
        try{
            db.execSQL("DELETE FROM camiones");
        } finally {
            db.close();
        }
    }

    static ArrayList<String> getArrayListPlacas(Context context, Boolean sync) {
        ArrayList<String> data = new ArrayList<>();
        String query;
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        if (sync) {
            query =  "SELECT camiones.* FROM camiones LEFT JOIN tags ON (camiones.idcamion = tags.idcamion) WHERE tags.idcamion IS NOT NULL ORDER BY economico ASC";
        } else {
            query = "SELECT camiones.* FROM camiones LEFT JOIN tags ON (camiones.idcamion = tags.idcamion) WHERE tags.idcamion IS NULL ORDER BY economico ASC";
        }
        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst())
            try {
                data.add("-- Seleccione --");
                data.add(c.getString(c.getColumnIndex("economico")) + " [" + c.getString(c.getColumnIndex("placas")) + "]");
                while (c.moveToNext()) {
                    data.add(c.getString(c.getColumnIndex("economico")) + " [" + c.getString(c.getColumnIndex("placas")) + "]");
                }
            } finally {
                c.close();
                db.close();
            }
        return data;
    }

    static ArrayList<String> getArrayListId(Context context, Boolean sync) {
        ArrayList<String> data = new ArrayList<>();
        String query;
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        if (sync) {
            query = "SELECT camiones.* FROM camiones LEFT JOIN tags ON (camiones.idcamion = tags.idcamion) WHERE tags.idcamion IS NOT NULL ORDER BY economico ASC";
        } else {
            query = "SELECT camiones.* FROM camiones LEFT JOIN tags ON (camiones.idcamion = tags.idcamion) WHERE tags.idcamion IS NULL ORDER BY economico ASC";
        }

        Cursor c = db.rawQuery(query, null);
        try {
            if (c != null && c.moveToFirst()) {
                data.add("0");
                data.add(c.getString(c.getColumnIndex("idcamion")));
                while (c.moveToNext()) {
                    data.add(c.getString(c.getColumnIndex("idcamion")));
                }
            }
            return data;
        } finally {
            c.close();
            db.close();
        }
    }

    public Integer getNumeroViajes(int idCamion){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT numero_viajes FROM camiones WHERE idcamion ='" + idCamion + "'", null);
        try{
            if(c!= null & c.moveToFirst()){
                return c.getInt(0);
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public Camion find(Integer idCamion) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT camiones.*,  tags.uid FROM camiones JOIN tags ON (camiones.idcamion = tags.idcamion) WHERE camiones.idcamion = '" + idCamion + "'", null);
        try {
            if(c != null && c.moveToFirst()) {
                this.idCamion   = c.getInt(c.getColumnIndex("idcamion"));
                this.placas     = c.getString(c.getColumnIndex("placas"));
                this.marca      = c.getString(c.getColumnIndex("marca"));
                this.modelo     = c.getString(c.getColumnIndex("modelo"));
                this.ancho      = c.getDouble(c.getColumnIndex("ancho"));
                this.largo      = c.getDouble(c.getColumnIndex("largo"));
                this.alto       = c.getDouble(c.getColumnIndex("alto"));
                this.economico  = c.getString(c.getColumnIndex("economico"));
                this.tag        = tag.find(c.getString(c.getColumnIndex("uid")));

                return this;
            } else {
                return null;
            }
        } finally {
            assert c != null;
            c.close();
            db.close();
        }
    }
}
