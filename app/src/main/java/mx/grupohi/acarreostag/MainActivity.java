package mx.grupohi.acarreostag;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
    private User user;
    private TagModel tags;
    private Camion camiones;
    private AlertDialog.Builder alertDialog;
    private NFCTag nfc;
    private Tag myTag;
    private NfcAdapter adapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private Intent SyncActivity;
    private String idCamion;
    boolean writeMode;
    Spinner  spinner ;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_activity_main);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        nfc = new NFCTag(myTag, this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

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
        camiones = new Camion(this);

        final ArrayList <String> placas = camiones.getArrayListPlacas();
        final ArrayList <String> ids = camiones.getArrayListId();

        String[] spinnerArray = new String[ids.size()];
        final HashMap<String,String> spinnerMap = new HashMap<>();

        for (int i = 0; i < ids.size(); i++) {
            spinnerMap.put(placas.get(i), ids.get(i));
            spinnerArray[i] = placas.get(i);
        }

        final ArrayAdapter<String> a = new ArrayAdapter<>(this,R.layout.text_layout, spinnerArray);
        a.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(a);

        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};

        checkNfcEnabled();

        Button btnWrite = (Button) findViewById(R.id.button_write);

        if(btnWrite != null)
        btnWrite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String placa = spinner.getSelectedItem().toString();
                idCamion = spinnerMap.get(placa);

                if(Objects.equals(idCamion, "0"))  {
                    Toast.makeText(MainActivity.this, getString(R.string.error_camion_no_selected), Toast.LENGTH_SHORT).show();
                } else {
                    checkNfcEnabled();
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
                myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                String UID = nfc.idTag(myTag);
                Log.i("UID", UID);

                if(tags.exists(UID)) {
                    if (tags.tagDisponible(UID)) {
                        mensaje = nfc.concatenar(idCamion, user.getIdProyecto());
                        nfc.writeID(myTag, 0, 1, mensaje);
                        tags.update(UID, idCamion, user.getIdProyecto());
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.error_tag_configurado), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.error_tag_inexistente), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onPause() {
        super.onPause();
        adapter.disableForegroundDispatch(this);
    }

    public void onResume() {
        super.onResume();
        checkNfcEnabled();
        if(writeMode) {
            adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        }
    }

    private void WriteModeOn() {
        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
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
