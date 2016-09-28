package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

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
    }

    public boolean create(JSONObject data) throws Exception {
        this.data.put("uid", data.getString("uid"));
        this.data.put("idcamion", data.getString("idcamion"));
        this.data.put("idproyecto", data.getString("idproyecto"));

        return db.insert("tags", null, this.data) > -1;
    }

    public void deleteAll() {
        db.execSQL("DELETE FROM tags");
    }
}
