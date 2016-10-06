package mx.grupohi.acarreostag;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SyncActivity extends AppCompatActivity {

    private Button syncButton;
    private ProgressDialog progressDialogSync;
    private JSONObject JSON;
    private Intent mainActivity;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_sync));
        setContentView(R.layout.activity_sync);

        user = new User(getApplicationContext());
        mainActivity = new Intent(this, MainActivity.class);

        syncButton = (Button) findViewById(R.id.SyncButton);

        if(syncButton != null)
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                    if (!TagModel.areSynchronized()) {
                        progressDialogSync = ProgressDialog.show(SyncActivity.this, "Sincronizando datos", "Por favor espere...", true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                new SyncTask().execute((Void) null);
                            }
                        }).run();
                    } else {
                        Toast.makeText(SyncActivity.this, "No es necesaria la sincronización en éste momento", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SyncActivity.this, R.string.error_internet, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    class SyncTask extends AsyncTask<Void, Void, Boolean> {

        JSONObject JSON;

        @Override
        protected Boolean doInBackground(Void... params) {

            ContentValues values = new ContentValues();

            values.put("metodo", getString(R.string.mnetodo_sync));
            values.put("usr", user.getUser());
            values.put("pass", user.getPass());
            values.put("bd", user.getBaseDatos());
            values.put("tag_camion", String.valueOf(TagModel.getJSON()));

            try {
                URL url = new URL("http://sca.grupohi.mx/android20160923.php");
                JSON = Util.JsonHttp(url, values);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage(getResources().getString(R.string.general_exception));
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialogSync.dismiss();
            if(aBoolean) {
                try {
                    if(JSON.has("error")) {
                        errorMessage((String) JSON.get("error"));
                    } else if (JSON.has("msj")) {
                        TagModel.sync();
                        new android.app.AlertDialog.Builder(SyncActivity.this)
                                .setTitle("¡Hecho!")
                                .setMessage((String) JSON.get("msj"))
                                .setCancelable(false)
                                .setPositiveButton(
                                        "Aceptar",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        startActivity(mainActivity);
                                                    }
                                                });
                                            }
                                        })
                                .create()
                                .show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void errorMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
