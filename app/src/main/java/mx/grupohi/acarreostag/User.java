package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import mx.grupohi.acarreostag.DBScaSqlite;

import org.json.JSONObject;

/**
 * Created by JFEsquivel on 27/09/2016.
 */

public class User {

    private Context context;

    private static SQLiteDatabase db;
    private static DBScaSqlite db_sca;

    public User(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    public boolean create(ContentValues values) {
        return db.insert("user", null, values) > -1;
    }

    public void deleteAll() {
        db.execSQL("DELETE FROM user");
    }

    public boolean get() {
        Cursor c = db.rawQuery("SELECT * FROM user", null);
        return c != null && c.moveToFirst();
    }
}
