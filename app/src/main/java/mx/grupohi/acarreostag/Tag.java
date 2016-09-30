package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

import java.security.PublicKey;

/**
 * Created by JFEsquivel on 28/09/2016.
 */

public class Tag {
    private Context context;
    private ContentValues data;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    public Tag(Context context) {
        this.context = context;
        this.data = new ContentValues();
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
        this.data.clear();
    }

    public boolean registrarTags(JSONObject data) throws Exception {
        this.data.clear();
        this.data.put("uid", data.getString("uid"));
        this.data.put("idcamion", data.getString("idcamion"));
        this.data.put("idproyecto", data.getString("idproyecto"));

        return db.insert("tags", null, this.data) > -1;
    }

    public void deleteAll() {
        db.execSQL("DELETE FROM tags");
        db.execSQL("DELETE FROM tags_disponibles");
    }

    public boolean registrarTagsDisponibles(JSONObject data) throws Exception {
        this.data.clear();
        this.data.put("uid", data.getString("uid"));
        this.data.put("idtag", data.getString("id"));
        this.data.put("idcamion", data.getString("idcamion"));

        return db.insert("tags_disponibles", null, this.data) > -1;
    }

    public boolean areSynchronized() {
        Cursor c = db.rawQuery("SELECT idcamion FROM tags_disponibles", null);
        Boolean result = true;
        try {
            if(c != null && c.moveToFirst()) {
                while (c.moveToNext()) {
                    if (c.getString(c.getColumnIndex("idcamion")) != null) {
                        result = false;
                    }
                }
            }
        } finally {
            c.close();
        }
        return result;
    }


}
