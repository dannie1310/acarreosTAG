package mx.grupohi.acarreostag;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class CambioClaveActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private ProgressDialog progressDialogSync;
    private ProgressDialog progressDialogCambio;
    private User usuario;
    private EditText uss;
    private EditText actual;
    private EditText pass;
    private EditText passConfirmacion;
    private Button cambio;
    private AlertDialog.Builder alertDialog;
    private String us_sesion;
    private String us_escrito;
    CambioClave c;
    //public String URL_API = "http://portal-aplicaciones.grupohi.mx/";
    public String URL_API = "http://192.168.0.183:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_clave);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        usuario = new User(this);
        usuario = usuario.getUsuario();
        us_sesion = usuario.user.toUpperCase();
        uss = (EditText) findViewById(R.id.user);
        pass = (EditText) findViewById(R.id.pass);
        passConfirmacion = (EditText)findViewById(R.id.passCambio);
        actual = (EditText) findViewById(R.id.passAnterior);
        cambio = (Button) findViewById(R.id.CambioButton);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        if (!Util.isNetworkStatusAvialable(getApplicationContext())) {
            Toast.makeText(CambioClaveActivity.this, R.string.error_internet, Toast.LENGTH_LONG).show();

        }

        cambio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!checar()){
                    us_escrito = uss.getText().toString().toUpperCase();
                    if (us_escrito.equals(us_sesion) && actual.getText().toString().equals(usuario.pass)) {

                        if (pass.getText().toString().length()>= 8 && passConfirmacion.getText().toString().length()>=8){

                            if(pass.getText().toString().equals(passConfirmacion.getText().toString())){
                                //OK
                                if (!Util.isNetworkStatusAvialable(getApplicationContext())) {
                                    Toast.makeText(CambioClaveActivity.this, R.string.error_internet, Toast.LENGTH_LONG).show();

                                }else {
                                    //OK
                                    progressDialogCambio = ProgressDialog.show(CambioClaveActivity.this, "Cambiando Contraseña", "Por favor espere...", true);
                                    c = new CambioClave(getApplicationContext(), progressDialogSync, pass.getText().toString());
                                    c.execute((Void) null);
                                }

                            }else{
                                Toast.makeText(getApplicationContext(), R.string.error_pass, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.error_field_requiredpass, Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        if (!us_escrito.equals(us_sesion)) {
                            Toast.makeText(getApplicationContext(), R.string.error_uss, Toast.LENGTH_SHORT).show();
                        } else if (!actual.getText().toString().equals(usuario.pass)) {
                            Toast.makeText(getApplicationContext(), R.string.error_anter, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        });

        if(drawer != null)
            drawer.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < drawer.getChildCount(); i++) {
                        View child = drawer.getChildAt(i);
                        TextView tvp = (TextView) child.findViewById(R.id.textViewProyecto);
                        TextView tvu = (TextView) child.findViewById(R.id.textViewUser);
                        TextView tvv = (TextView) child.findViewById(R.id.textViewVersion);

                        if (tvp != null) {
                            tvp.setText(usuario.descripcionBaseDatos);
                        }
                        if (tvu != null) {
                            tvu.setText(usuario.name);
                        }
                        if (tvv != null) {
                            tvv.setText(getString(R.string.app_name)+"     "+"Versión " + String.valueOf(BuildConfig.VERSION_NAME));
                        }
                    }
                }
            });
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private boolean checar() {

        //Reset Errors

        //sindicato.setError(null);
        uss.setError(null);
        pass.setError(null);
        passConfirmacion.setError(null);
        actual.setError(null);

        final String usuarios = uss.getText().toString();
        final String passw = pass.getText().toString();
        final String passCambio = passConfirmacion.getText().toString();
        final String vieja = actual.getText().toString();


        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(usuarios)) {
            uss.setError(getString(R.string.error_field_required));
            focusView = uss;
            cancel = true;
        }
        if(TextUtils.isEmpty(vieja)) {
            actual.setError(getString(R.string.error_field_required));
            focusView = actual;
            cancel = true;
        }
        if(TextUtils.isEmpty(passw)) {
            pass.setError(getString(R.string.error_field_required));
            focusView = pass;
            cancel = true;
        }
        if(TextUtils.isEmpty(passCambio)) {
            passConfirmacion.setError(getString(R.string.error_field_required));
            focusView = passConfirmacion;
            cancel = true;
        }

        return cancel;

    }




    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_logout) {
            if (TagModel.areSynchronized(getApplicationContext())) {
                usuario.deleteAll();
                Intent loginActivity = new Intent(this, LoginActivity.class);
                startActivity(loginActivity);
            } else {
                alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Advertencia")
                        .setMessage("No es posible cerrar la sesión ya que aún no haz sincronizado los tags configurados")
                        .setPositiveButton("¡Sincronizar Ahora!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                nextActivity();
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        } else if (id == R.id.nav_sync) {
            nextActivity();
        } else if (id == R.id.nav_desc) {
            Intent descarga = new Intent(this, DescargaActivity.class);
            startActivity(descarga);

        } else if (id == R.id.nav_replace) {
            Intent intent = new Intent(CambioClaveActivity.this,  ReemplazarActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_inicio) {
            Intent intent = new Intent(CambioClaveActivity.this,  MainActivity.class);
            startActivity(intent);
        } else if(id == R.id.nav_cambio){
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }
    private void nextActivity() {
         Intent SyncActivity = new Intent(this, SyncActivity.class);
        startActivity(SyncActivity);
    }

    public class CambioClave extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private ProgressDialog progressDialog;
        private User usuario;


        private String IMEI;
        private String NuevaClave;

        private JSONObject JSONVIAJES;
        private JSONObject JSON;
        Intent in;

        CambioClave(Context context, ProgressDialog progressDialog, String clavenueva) {

            this.context = context;
            this.progressDialog = progressDialog;
            this.NuevaClave = clavenueva;
            usuario = new User(context);
            usuario = usuario.getUsuario();

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            TelephonyManager phneMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            IMEI = phneMgr.getDeviceId();
            in = new Intent(context, CambioClaveActivity.class);
            ContentValues values = new ContentValues();
            Boolean resp = null;
            values.clear();

            values.put("usuario", usuario.user);
            values.put("clave", usuario.pass);
            values.put("idusuario", usuario.getId());
            values.put("bd", usuario.base);
            values.put("IMEI", IMEI);
            values.put("Version", String.valueOf(BuildConfig.VERSION_NAME));
            values.put("NuevaClave", NuevaClave);

            try {

                URL url = new URL(URL_API + "api/acarreos/tag/cambioClave?access_token=" + usuario.token);
                JSONVIAJES = HttpConnection.POST(url, values);
                Log.i("josn", String.valueOf(JSONVIAJES));
                Log.i("jsonviajes:  ", String.valueOf(values));
                if (JSONVIAJES.has("error")) {
                    Toast.makeText(context, (String) JSONVIAJES.get("error"), Toast.LENGTH_SHORT).show();
                    resp = false;
                } else if (JSONVIAJES.has("msj")) {
                    User.updatePass(NuevaClave, context);
                    Toast.makeText(context, (String) JSONVIAJES.get("msj"), Toast.LENGTH_LONG).show();
                    resp = true;
                }

            } catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                resp =  false;
            }
            return resp;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            c = null;
            progressDialogCambio.dismiss();
            if (aBoolean) {
                try {
                    Toast.makeText(context, (String) JSONVIAJES.get("msj"), Toast.LENGTH_LONG).show();
                    //startActivity(in);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(in);
            }else{
                Toast.makeText(context, "Error al cambiar la contraseña, verifique su conexión.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
