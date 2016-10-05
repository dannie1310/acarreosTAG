package mx.grupohi.acarreostag;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Intent loginActivity;
    private Button btnWrite;
    private FloatingActionButton cancel;
    private Spinner  spinner ;
    private HashMap<String, String> spinnerMap;

    private User user;
    private TagModel tags;
    private Camion camiones;
    private AlertDialog.Builder alertDialog;
    private TextView infoCamion;
    private ProgressDialog progress;
    private NFCTag nfc;
    private NfcAdapter adapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private Intent SyncActivity;
    private String idCamion;
    private AlertDialog alerta;
    private boolean writeMode;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_activity_main);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        infoCamion = (TextView) findViewById(R.id.textViewInfoCamion);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if(drawer != null)
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if(navigationView != null)
        navigationView.setNavigationItemSelectedListener(this);

        user = new User(this);
        tags = new TagModel(this);

        loginActivity = new Intent(this, LoginActivity.class);
        if(drawer != null)
        drawer.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < drawer.getChildCount(); i++) {
                    View child = drawer.getChildAt(i);
                    TextView tvp = (TextView) child.findViewById(R.id.textViewProyecto);
                    TextView tvu = (TextView) child.findViewById(R.id.textViewUser);

                    if (tvp != null) {
                        tvp.setText(user.getProyecto());
                    }
                    if (tvu != null) {
                        tvu.setText(user.getName());
                    }
                }
            }
        });


        spinner = (Spinner) findViewById(R.id.spinner);
        if(spinner != null) {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    String placa = spinner.getSelectedItem().toString();
                    idCamion = spinnerMap.get(placa);
                    setInfoCamion(idCamion);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        camiones = new Camion(this);

        final ArrayList <String> placas = camiones.getArrayListPlacas();
        final ArrayList <String> ids = camiones.getArrayListId();

        String[] spinnerArray = new String[ids.size()];
        spinnerMap = new HashMap<>();

        for (int i = 0; i < ids.size(); i++) {
            spinnerMap.put(placas.get(i), ids.get(i));
            spinnerArray[i] = placas.get(i);
        }

        final ArrayAdapter<String> a = new ArrayAdapter<>(this,R.layout.text_layout, spinnerArray);
        a.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(a);

        adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter == null) {
            Toast.makeText(this, getString(R.string.error_no_nfc), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        checkNfcEnabled();

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        //tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};


        btnWrite = (Button) findViewById(R.id.button_write);

        if(btnWrite != null)
        btnWrite.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

              /*  AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                alert.setMessage("Acerca el Tag")
                        .setTitle("")
                        .setIcon(R.drawable.ic_sync_black_24dp)
                        .setInverseBackgroundForced(true)
                        .setCancelable(false)
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                btnWrite.setEnabled(true);
                                spinner.setEnabled(true);
                                WriteModeOff();
                                dialog.cancel();
                            }
                    });*/


                if(Objects.equals(idCamion, "0"))  {
                    Toast.makeText(MainActivity.this, getString(R.string.error_camion_no_selected), Toast.LENGTH_SHORT).show();
                } else {

                    checkNfcEnabled();


                   progress=new ProgressDialog(MainActivity.this);
                    progress.setMessage("Acerque el Tag para Configurarlo");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setIndeterminate(true);
                    progress.setProgress(50);
                    progress.setCancelable(false);
                    progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            btnWrite.setEnabled(true);
                            spinner.setEnabled(true);
                            WriteModeOff();
                            dialog.dismiss();
                        }
                    });
                    progress.show();
                    WriteModeOn();
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String mensaje;
        if(writeMode) {
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                nfc = new NFCTag(myTag, this);

                String UID = nfc.idTag(myTag);
                Log.i("UID", UID);
                if (tags.exists(UID)) {

                    if (tags.tagDisponible(UID)) {

                        mensaje = nfc.concatenar(idCamion, user.getIdProyecto());
                        nfc.writeID(myTag, 0, 1, mensaje);
                        tags.update(UID, idCamion);
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.error_tag_configurado), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.error_tag_inexistente), Toast.LENGTH_SHORT).show();
                }

                progress.cancel();
                btnWrite.setEnabled(true);
                spinner.setEnabled(true);




            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNfcEnabled();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.disableForegroundDispatch(this);
    }
    private void WriteModeOn() {
        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        btnWrite.setEnabled(false);
        spinner.setEnabled(false);
    }

    private void WriteModeOff() {
        writeMode = false;
        adapter.disableForegroundDispatch(this);
    }


    @TargetApi(Build.VERSION_CODES.DONUT)
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            if (tags.areSynchronized()) {
                user.deleteAll();
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

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void nextActivity() {
        SyncActivity = new Intent(this, SyncActivity.class);
        startActivity(SyncActivity);
    }

    private void setInfoCamion(String idCamion) {
        if (idCamion != "0") {
            Cursor c = Camion.get(idCamion);
            c.moveToFirst();
            infoCamion.setText(
                    Html.fromHtml("<h1>Camion:" +
                            c.getString(
                                    c.getColumnIndex("economico")
                            ) +
                            "</h1><font color=\"#A0A0A0\">placas: </font> " +
                            c.getString(
                                    c.getColumnIndex("placas")
                            ) +
                            "<br><font color=\"#A0A0A0\">modelo: </font>" +
                            c.getString(
                                    c.getColumnIndex("modelo")
                            ) +
                            "<br><font color=\"#A0A0A0\">Ancho: </font>" +
                            c.getString(
                                    c.getColumnIndex("ancho")
                            ) +
                            "<br><font color=\"#A0A0A0\">Alto: </font>" +
                            c.getString(
                                    c.getColumnIndex("alto")
                            ) +
                            "<br><font color=\"#A0A0A0\">Largo: </font>" +
                            c.getString(
                                    c.getColumnIndex("largo")
                            ) +
                            ""
                    )

            );
        } else {
            infoCamion.setText("");
        }
    }
    private void checkNfcEnabled() {
        Boolean nfcEnabled = adapter.isEnabled();
        if (!nfcEnabled) {
            new android.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.text_warning_nfc_is_off))
                    .setMessage(getString(R.string.text_turn_on_nfc))
                    .setCancelable(true)
                    .setPositiveButton(
                            getString(R.string.text_update_settings),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            })
                    .create()
                    .show();
        }
    }
}
