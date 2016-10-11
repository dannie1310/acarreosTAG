package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by JFEsquivel on 28/09/2016.
 */

class Camion {

    private static android.database.sqlite.SQLiteDatabase db;
    private Context context;
    private ContentValues data;

    private DBScaSqlite db_sca;

     Camion(Context context) {
        this.context = context;
        data = new ContentValues();
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    public static Cursor get(String idCamion) {
        Cursor c = db.rawQuery("SELECT * FROM camiones WHERE idcamion = '" + idCamion + "'", null);
        return c;
    }
    boolean create(JSONObject data) throws Exception {

        Log.i("JSON", data.toString());
        this.data.put("idCamion", data.getString("idcamion"));
        this.data.put("placas", data.getString("placas"));
        this.data.put("marca", data.getString("marca"));
        this.data.put("modelo", data.getString("modelo"));
        this.data.put("ancho", data.getString("ancho"));
        this.data.put("largo", data.getString("largo"));
        this.data.put("alto", data.getString("alto"));
        this.data.put("economico", data.getString("economico"));

        return db.insert("camiones", null, this.data) > -1;
    }

    void deleteAll() {
        db.execSQL("DELETE FROM camiones");
    }

    ArrayList<String> getArrayListPlacas() {
        ArrayList<String> data = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT camiones.* FROM camiones LEFT JOIN tags ON (camiones.idcamion = tags.idcamion) WHERE tags.idcamion IS NULL ORDER BY economico ASC", null);
        if (c != null && c.moveToFirst())
            try {
                data.add("-- Seleccione --");
                while (c.moveToNext()) {
                    data.add(c.getString(c.getColumnIndex("economico")) + " [" + c.getString(c.getColumnIndex("placas")) + "]");
                }
            } finally {
                c.close();
            }
        return data;
    }

    ArrayList<String> getArrayListId() {
        ArrayList<String> data = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT camiones.* FROM camiones LEFT JOIN tags ON (camiones.idcamion = tags.idcamion) WHERE tags.idcamion IS NULL ORDER BY economico ASC", null);
        if (c != null && c.moveToFirst())
            try {
                data.add("0");
                while (c.moveToNext()) {
                    data.add(c.getString(c.getColumnIndex("idcamion")));
                }
            } finally {
                c.close();
            }
        return data;
    }
}
