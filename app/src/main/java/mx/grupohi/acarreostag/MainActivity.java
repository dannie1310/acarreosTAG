package mx.grupohi.acarreostag;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import mx.grupohi.acarreostag.MD5;

/**
 * Pantalla de Login por medio de datos de Intranet.
 */
public class MainActivity extends AppCompatActivity  {

    private UserLoginTask mAuthTask = null;

    DBScaSqlite db_sca;
    SQLiteDatabase db;

    // Referencias UI.
    private AutoCompleteTextView mUsuarioView;
    private TextInputLayout formLayout;
    private EditText mPasswordView;
    private ProgressDialog mProgressDialog;
    private Button mIniciarSesionButton;
    Intent configuracionTAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsuarioView = (AutoCompleteTextView) findViewById(R.id.usuario);
        mPasswordView = (EditText) findViewById(R.id.password);

        formLayout = (TextInputLayout) findViewById(R.id.form_layout);
        mIniciarSesionButton = (Button) findViewById(R.id.iniciar_sesion_button);
        mIniciarSesionButton.setOnClickListener(buttonListener);

        db_sca = new DBScaSqlite(this, "sca", null, 1);

        configuracionTAG = new Intent(this, ConfTAG.class);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isAuth()) {
            startActivity(configuracionTAG);
        }
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isNetworkStatusAvialable(getApplicationContext())) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            attemptLogin();
                        } catch (Exception e) {
                            errorMessage(getString(R.string.error_login));
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                Toast.makeText(MainActivity.this, R.string.error_internet, Toast.LENGTH_LONG).show();
            }
        }
    };

    public static boolean isNetworkStatusAvialable (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if(netInfos != null)
                if(netInfos.isConnected())
                    return true;
        }
        return false;
    }

    private boolean isAuth() {
        db = db_sca.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT iduser, nombre, usr, pass, idproyecto, base_datos, descripcion_database FROM user limit 1 ", null);
        if(c != null) {
            if (c.moveToFirst()) {
                return true;
            }
        } return false;
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

        // Store values at the time of the login attempt.
        final String usuario = mUsuarioView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(!TextUtils.isEmpty(usuario)) {
            mUsuarioView.setError(getString(R.string.error_field_required));
            focusView = mUsuarioView;
            cancel = true;
        }

        if(!TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mProgressDialog = ProgressDialog.show(MainActivity.this, "Autenticando", "Por favor espere...", true);
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
        db.execSQL("DELETE FROM user;");
        db.execSQL("DELETE FROM camiones;");
        db.execSQL("DELETE FROM tags;");
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
            //values.put("pass", new MD5(mPassword).convert());
            values.put("pass", mPassword);

            try {
                URL url = new URL("http://sca.grupohi.mx/android20160923.php");
                JSON = JsonHttp(url, values);
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
                    db = db_sca.getWritableDatabase();

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
            mProgressDialog.dismiss();
            if (aBoolean) {
                startActivity(configuracionTAG);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    private void updateCatalogos(JSONObject JSON) {

    }

    private JSONObject JsonHttp(URL url, ContentValues values) throws JSONException {
        InputStream is = null;
        StringBuilder sb = null;
        String response = "";
        OutputStream os = null;

        try {

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            os = conn.getOutputStream();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write(getQuery(values));
            bw.flush();

            int statusCode = conn.getResponseCode();

            Log.i("Status Code",String.valueOf(statusCode));

            is = conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            sb = new StringBuilder();
            sb.append(br.readLine() + "\n");
            String line="0";
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            response = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage(getResources().getString(R.string.general_exception));
        }
        return  new JSONObject(response);
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

    private String getQuery(ContentValues values) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> entry : values.valueSet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        }
        return result.toString();
    }
}

