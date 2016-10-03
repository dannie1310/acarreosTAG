package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Contacts;
import android.widget.Toast;

import org.json.JSONObject;

import java.security.PublicKey;
import java.util.Objects;

/**
 * Creado por JFEsquivel on 28/09/2016.
 */

class TagModel {
    private Context context;
    private ContentValues data;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    TagModel(Context context) {
        this.context = context;
        this.data = new ContentValues();
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
        this.data.clear();
    }

    boolean registrarTags(JSONObject data) throws Exception {
        this.data.clear();
        this.data.put("uid", data.getString("uid"));
        this.data.put("idcamion", data.getString("idcamion"));
        this.data.put("idproyecto", data.getString("idproyecto"));

        return db.insert("tags", null, this.data) > -1;
    }

    void deleteAll() {
        db.execSQL("DELETE FROM tags");
        db.execSQL("DELETE FROM tags_disponibles");
    }

    boolean registrarTagsDisponibles(JSONObject data) throws Exception {
        this.data.clear();
        this.data.put("uid", data.getString("uid"));
        this.data.put("idtag", data.getString("id"));
        this.data.put("idcamion", !Objects.equals(data.getString("idcamion"), "null") ? data.getString("idcamion") : null);

        return db.insert("tags_disponibles", null, this.data) > -1;
    }

    boolean areSynchronized() {
        Boolean result = true;
        try (Cursor c = db.rawQuery("SELECT idcamion FROM tags_disponibles", null)) {
            if (c != null && c.moveToFirst()) {
                while (c.moveToNext()) {
                    if (c.getString(c.getColumnIndex("idcamion")) != null) {
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    boolean exists(String UID) {
        boolean result;
        try (Cursor c = db.rawQuery("SELECT * FROM (SELECT uid FROM tags UNION SELECT uid FROM tags_disponibles) as total WHERE uid = '" + UID + "'", null)) {
            result = c != null && c.moveToFirst();
        }
        return  result;
    }

    void update(String UID, String idcamion, String idProyecto) {
        this.data.clear();
        this.data.put("uid", UID);
        this.data.put("idcamion", idcamion);
        this.data.put("idproyecto", idProyecto);

        try {
            db.delete("tags_disponibles", "uid = '" + UID + "'", null);
            db.insert("tags", null, this.data);
        } catch (Exception e) {

            Toast.makeText(this.context, this.context.getString(R.string.error_update_tag), Toast.LENGTH_SHORT).show();
        }
    }

    boolean tagDisponible (String UID) {
        boolean result;
        try (Cursor c = db.rawQuery("SELECT * FROM tags_disponibles WHERE uid = '" + UID + "' and idcamion is null", null)) {
            result = c != null && c.moveToFirst();
        }
        return result;
    }
}
