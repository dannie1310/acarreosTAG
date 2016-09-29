package mx.grupohi.acarreostag;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import mx.grupohi.acarreostag.NFCTag;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Intent loginActivity;
    private User user;

    private static final int N_ITEMS = 10;
    String text;
    Tag myTag;
    String lectura;
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    ViewGroup main;
    boolean writeMode;
    Spinner  spinner ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*spinner = (Spinner) findViewById(R.id.spinner);
        addItemsSpinnerDinamico();*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        user = new User(this);
        loginActivity = new Intent(this, LoginActivity.class);

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


        Button btnWrite= (Button) findViewById(R.id.button_write);
        final TextView message = (TextView) findViewById(R.id.texto);

        btnWrite.setOnClickListener(new View.OnClickListener() {
            NFCTag nfc = new NFCTag(myTag, MainActivity.this);
            @Override
            public void onClick(View v) {
                text = message.getText().toString();


                try {

                    if (myTag == null) {
                        Toast.makeText(MainActivity.this, "error tag", Toast.LENGTH_LONG).show();
                    } else {
                        nfc.write(text, myTag);  //escribe mensaje en el Tag
                        //lectura = nfc.read(myTag);    // lee los datos de la tag*/
                        // lectura=readSector(myTag,3); //lee solo un sector
                        // lectura = idTag(myTag); //muestra el UID de la tarjeta
                        //System.out.println(lectura);
                        //Toast.makeText(context, "UID: " + lectura, Toast.LENGTH_LONG).show();

                        //clean(myTag);  // limpia la memoria del Tag
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "error de escritura", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};
    }

    private void addItemsSpinnerDinamico() {

            List<String> dynamicList = new ArrayList<String>();
            for (int i = 0; i < N_ITEMS; i++) {
                dynamicList.add("item dynamic " + i);
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.content_main, dynamicList);
            dataAdapter.setDropDownViewResource(R.layout.content_main);

        spinner.setAdapter(dataAdapter);


    }

    @SuppressLint("NewApi")
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang = "us";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payLoad = new byte[1 + langLength + textLength];

        payLoad[0] = (byte) langLength;

        System.arraycopy(langBytes, 0, payLoad, 1, langLength);
        System.arraycopy(textBytes, 0, payLoad, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payLoad);

        return recordNFC;

    }


    @SuppressLint("NewApi")
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Toast.makeText(MainActivity.this, "detectado", Toast.LENGTH_LONG).show();

        }
    }


    public void onPause() {
        super.onPause();
        WriteModeOff();
    }

    public void onResume() {
        super.onResume();
        WriteModeOn();
    }

    @SuppressLint("NewApi")
    private void WriteModeOn() {
        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    @SuppressLint("NewApi")
    private void WriteModeOff() {
        writeMode = false;
        adapter.disableForegroundDispatch(this);
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            if (isSync()) {
                user.deleteAll();
                startActivity(loginActivity);
            }
            // CODIGO PARA VERIFICAR QUE SE HAN SINCRONIZADO LOS CAMBIOS EN EL SERVIDOR, SI SE HAN SINCRONIZADO SE CIERRA SESIÓN
            user.deleteAll();
        } else if (id == R.id.nav_sync) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isSync() {
        return true;
    }
}
