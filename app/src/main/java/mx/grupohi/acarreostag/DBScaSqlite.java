package mx.grupohi.acarreostag;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.PublicKey;

/**
 * Created by JFEsquivel on 26/09/2016.
 */

public class DBScaSqlite extends SQLiteOpenHelper {

    private String [] queries = new String [] {
            "CREATE TABLE user (idusuario INTEGER, nombre TEXT, usr TEXT, pass TEXT, idproyecto INTEGER, base_datos TEXT, descripcion_database TEXT)",
            "CREATE TABLE camiones (idcamion INTEGER, placas TEXT, marca TEXT, modelo TEXT, ancho REAL, largo REAL, alto REAL, economico TEXT)",
            "CREATE TABLE tags (uid TEXT PRIMARY KEY, idcamion INTEGER, idproyecto INTEGER, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, estado INTEGER DEFAULT 1)"
    };

    public DBScaSqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String query : queries) {
            db.execSQL(query);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS camiones");
        db.execSQL("DROP TABLE IF EXISTS tags");
        db.execSQL("DROP TABLE IF EXISTS tags");

        for (String query : queries) {
            db.execSQL(query);
        }
    }
}
