package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by JFEsquivel on 28/09/2016.
 */

public class Camion {

    Context context;
    ContentValues data;

    SQLiteDatabase db;
    DBScaSqlite db_sca;

    public Camion(Context context) {
        this.context = context;
        data = new ContentValues();
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    public boolean create(JSONObject data) throws Exception {

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

    public void deleteAll() {
        db.execSQL("DELETE FROM camiones");
    }
}
