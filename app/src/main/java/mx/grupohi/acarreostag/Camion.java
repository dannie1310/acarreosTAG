package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Creado por JFEsquivel el 28/09/2016.
 */

class Camion {

    private SQLiteDatabase db;
    private Context context;
    private ContentValues data;

    private DBScaSqlite db_sca;

     Camion(Context context) {
        this.context = context;
        data = new ContentValues();
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
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

    ArrayList<String> getArrayListPlacas() {
        ArrayList<String> data = new ArrayList<>();
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT camiones.* FROM camiones LEFT JOIN tags ON (camiones.idcamion = tags.idcamion) WHERE tags.idcamion IS NULL ORDER BY economico ASC", null);
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

    ArrayList<String> getArrayListId() {
        ArrayList<String> data = new ArrayList<>();
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT camiones.* FROM camiones LEFT JOIN tags ON (camiones.idcamion = tags.idcamion) WHERE tags.idcamion IS NULL ORDER BY economico ASC", null);
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
}
