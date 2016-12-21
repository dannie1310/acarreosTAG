package mx.grupohi.acarreostag;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

public class DescargaActivity extends AppCompatActivity {


    private ProgressDialog progressDialogSync;
    private TextInputLayout loginFormLayout;
    private ProgressDialog loginProgressDialog;
    User usuario;
    Camion camion;
    TagModel tag;

    private Intent mainActivity;

    private DescargaCatalogos descargaCatalogos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descarga);
        camion = new Camion(getApplicationContext());
        tag = new TagModel(getApplicationContext());
        usuario = new User(this);
        mainActivity = new Intent(this, MainActivity.class);
        loginProgressDialog = ProgressDialog.show(DescargaActivity.this, "Descargando", "Por favor espere...", true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                descargaCatalogos = new DescargaActivity.DescargaCatalogos(getApplicationContext(), loginProgressDialog);
                descargaCatalogos.execute((Void) null);
            }
        }).run();

    }

    public void deleteAllTables() {
        camion.deleteAll();
        tag.deleteAll();
    }

    public class DescargaCatalogos extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private ProgressDialog progressDialog;
        private User usuario;
        private DBScaSqlite db_sca;
        private JSONObject JSON;


        DescargaCatalogos(Context context, ProgressDialog progressDialog) {
            this.context = context;
            this.progressDialog = progressDialog;
            usuario = new User(context);
            db_sca = new DBScaSqlite(context, "sca", null, 1);
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            ContentValues data = new ContentValues();
            data.put("metodo", "ConfDATA");
            data.put("usr", usuario.getUsr());
            data.put("pass", usuario.getPass());
            try {
                URL url = new URL("http://sca.grupohi.mx/android20160923.php");
                JSON = Util.JsonHttp(url, data);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            deleteAllTables();
            try {
                if (JSON.has("error")) {
                    errorMessage((String) JSON.get("error"));
                    return false;
                } else {
                    try {
                        final JSONArray camiones = new JSONArray(JSON.getString("Camiones"));
                        for (int i = 0; i < camiones.length(); i++) {
                            final int finalI = i + 1;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setMessage("Actualizando catálogo de camiones... \n Camion " + finalI + " de " + camiones.length());
                                }
                            });
                            camion.create(camiones.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        final JSONArray tags = new JSONArray(JSON.getString("tags"));
                        for (int i = 0; i < tags.length(); i++) {
                            final int finalI = i + 1;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setMessage("Actualizando catálogo de Tags... \n Tag " + finalI + " de " + tags.length());
                                }
                            });
                            //System.out.println("Tags: " + tags.getJSONObject(i));
                            tag.registrarTags(tags.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        final JSONArray tags_disponibles = new JSONArray(JSON.getString("tags_disponibles_configurar"));
                        for (int i = 0; i < tags_disponibles.length(); i++) {
                            final int finalI = i + 1;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setMessage("Actualizando catálogo de Tags Configurables... \n Tag " + finalI + " de " + tags_disponibles.length());
                                }
                            });
                            //System.out.println("TagsDisponibles: " + tags_disponibles.getJSONObject(i));
                            tag.registrarTagsDisponibles(tags_disponibles.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage(getResources().getString(R.string.general_exception));
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            descargaCatalogos = null;
            loginProgressDialog.dismiss();
            if (success) {
                startActivity(mainActivity);
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