package mx.grupohi.acarreostag;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Pantalla de Login por medio de datos de Intranet.
 */
public class LoginActivity extends AppCompatActivity  {

    private UserLoginTask mAuthTask = null;

    DBScaSqlite db_sca;
    SQLiteDatabase db;
    User user;
    Camion camion;
    TagModel tag;

    // Referencias UI.
    private AutoCompleteTextView mUsuarioView;
    private TextInputLayout formLayout;
    private EditText mPasswordView;
    private ProgressDialog mProgressDialog;
    private Button mIniciarSesionButton;
    Intent mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = new User(this);
        if(user.get()) {
            nextActivity();
        }
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_login_activity);
        setContentView(R.layout.activity_login);

        mUsuarioView = (AutoCompleteTextView) findViewById(R.id.usuario);
        mPasswordView = (EditText) findViewById(R.id.password);

        formLayout = (TextInputLayout) findViewById(R.id.layout);
        mIniciarSesionButton = (Button) findViewById(R.id.iniciar_sesion_button);

        camion = new Camion(this);
        tag = new TagModel(this);

        mIniciarSesionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                    attemptLogin();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.error_internet, Toast.LENGTH_LONG).show();
                }
            }
        });

        db_sca = new DBScaSqlite(this, "sca", null, 1);
    }

    @Override
    protected void onStart() {

        super.onStart();
        if(user.get()) {
            nextActivity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void nextActivity() {
        mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        //Reset Errors
        mUsuarioView.setError(null);
        mPasswordView.setError(null);
        formLayout.setError(null);

        // Store values at the time of the login attempt.
        final String usuario = mUsuarioView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if(TextUtils.isEmpty(usuario)) {
            mUsuarioView.setError(getString(R.string.error_field_required));
            focusView = mUsuarioView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mProgressDialog = ProgressDialog.show(LoginActivity.this, "Autenticando", "Por favor espere...", true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mAuthTask = new UserLoginTask(usuario, password);
                    mAuthTask.execute((Void) null);
                }
            }).run();
        }
    }

    public void deleteAllTables() {
        user.deleteAll();
        camion.deleteAll();
        tag.deleteAll();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsuario;
        private final String mPassword;
        private JSONObject JSON;

        UserLoginTask(String email, String password) {
            mUsuario = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ContentValues values = new ContentValues();

            values.put("metodo", "ConfDATA");
            values.put("usr", mUsuario);
            values.put("pass", mPassword);

            try {
                URL url = new URL("http://sca.grupohi.mx/android20160923.php");
                JSON = Util.JsonHttp(url, values);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage(getResources().getString(R.string.general_exception));
                return false;
            }
            deleteAllTables();
            try {
                if(JSON.has("error")) {
                    errorLayout(formLayout, (String) JSON.get("error"));
                    return false;
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.setTitle("Actualizando");
                            mProgressDialog.setMessage("Actualizando datos de usuario...");
                        }
                    });

                    ContentValues data = new ContentValues();

                    data.put("idusuario", (String) JSON.get("IdUsuario"));
                    data.put("nombre", (String) JSON.get("Nombre"));
                    data.put("usr", mUsuario);
                    data.put("pass", mPassword);
                    data.put("idproyecto", (String) JSON.get("IdProyecto"));
                    data.put("base_datos", (String) JSON.get("base_datos"));
                    data.put("descripcion_database", (String) JSON.get("descripcion_database"));

                    user.create(data);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.setMessage("Actualizando catálogo de tags...");
                        }
                    });

                    try {
                        final JSONArray camiones = new JSONArray(JSON.getString("Camiones"));
                        for (int i = 0; i < camiones.length(); i++) {
                            final int finalI = i + 1;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.setMessage("Actualizando catálogo de camiones... \n Camion " + finalI + " de " + camiones.length());
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
                                    mProgressDialog.setMessage("Actualizando catálogo de Tags... \n Tag " + finalI + " de " + tags.length());
                                }
                            });
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
                                    mProgressDialog.setMessage("Actualizando catálogo de Tags Configurables... \n Tag " + finalI + " de " + tags_disponibles.length());
                                }
                            });
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
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mAuthTask = null;
            mProgressDialog.dismiss();
            if (aBoolean) {
                nextActivity();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
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

    private void errorLayout(final TextInputLayout layout, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layout.setErrorEnabled(true);
                layout.setError(message);
            }
        });
    }


}

