package mx.grupohi.acarreostag;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class ReemplazarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private User user;
    private String idCamion;
    private Spinner camionesSpinner;
    private HashMap<String, String> spinnerMap;
    private Camion camion;

    private Button cambiarButton;
    private TextView infoCamion;
    private TextView mainTitle;
    private ImageView nfcImage;
    private FloatingActionButton fabCancel;

    private NFCTag nfc;
    private NFCUltralight nfcUltra;
    private NfcAdapter adapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private boolean writeMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = new User(this);
        camion = new Camion(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reemplazar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        infoCamion = (TextView) findViewById(R.id.txtCamion);
        mainTitle = (TextView) findViewById(R.id.mainTitle);
        nfcImage = (ImageView) findViewById(R.id.nfc_background);
        nfcImage.setVisibility(View.GONE);

        fabCancel = (FloatingActionButton) findViewById(R.id.fabCancel);
        fabCancel.setVisibility(View.GONE);
        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteModeOff();
            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
                            tvp.setText(User.getProyecto(getApplicationContext()));
                        }
                        if (tvu != null) {
                            tvu.setText(user.getName());
                        }
                        if (tvv != null) {
                            tvv.setText("Versión " + String.valueOf(BuildConfig.VERSION_NAME));
                        }
                    }
                }
            });
        camionesSpinner = (Spinner) findViewById(R.id.spinnerCamiones);

        final ArrayList<String> placas = Camion.getArrayListPlacas(getApplicationContext(), true);
        final ArrayList <String> ids = Camion.getArrayListId(getApplicationContext(), true);

        String[] spinnerArray = new String[ids.size()];
        spinnerMap = new HashMap<>();

        for (int i = 0; i < ids.size(); i++) {
            spinnerMap.put(placas.get(i), ids.get(i));
            spinnerArray[i] = placas.get(i);
        }

        final ArrayAdapter<String> a = new ArrayAdapter<>(this,R.layout.text_layout, spinnerArray);
        a.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        camionesSpinner.setAdapter(a);

        camionesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String placa = camionesSpinner.getSelectedItem().toString();
                idCamion = spinnerMap.get(placa);
                setInfoCamion(idCamion);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter == null) {
            Toast.makeText(this, getString(R.string.error_no_nfc), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        checkNfcEnabled();

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        writeTagFilters = new IntentFilter[]{tagDetected};

        cambiarButton = (Button) findViewById(R.id.buttonCambiarTag);
        cambiarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(idCamion == "0") {
                    Toast.makeText(ReemplazarActivity.this, getString(R.string.error_camion_no_selected), Toast.LENGTH_SHORT).show();
                } else {
                    new android.app.AlertDialog.Builder(ReemplazarActivity.this)
                            .setTitle("INFORMACIÓN")
                            .setMessage("Una vez reemplazado el TAG del camión " + camion.economico + " se cambiara la configuración actual.\n ¡No olvide sincronizar los cambios para que el reemplazo tenga efecto!")
                            .setCancelable(true)
                            .setPositiveButton("Reemplazar TAG",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            checkNfcEnabled();
                                            WriteModeOn();
                                        }
                                    })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                    }
            }
        });

        new android.app.AlertDialog.Builder(ReemplazarActivity.this)
                .setMessage("En ésta sección puede reemplazar el TAG de los camiones previamente configurados y sincronizados")
                .setNeutralButton("ENTENDIDO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
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

    @Override
    public void onResume() {
        super.onResume();
        checkNfcEnabled();
        WriteModeOff();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.disableForegroundDispatch(this);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            if (TagModel.areSynchronized(getApplicationContext())) {
                user.deleteAll();
                Intent loginActivity = new Intent(ReemplazarActivity.this, LoginActivity.class);
                startActivity(loginActivity);
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Advertencia")
                        .setMessage("No es posible cerrar la sesión ya que aún no haz sincronizado los tags configurados")
                        .setPositiveButton("¡Sincronizar Ahora!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent syncActivity = new Intent(ReemplazarActivity.this, SyncActivity.class);
                                startActivity(syncActivity);
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        } else if (id == R.id.nav_sync) {
            Intent syncActivity = new Intent(ReemplazarActivity.this, SyncActivity.class);
            startActivity(syncActivity);
        } else if (id == R.id.nav_desc) {
            Intent descarga = new Intent(this, DescargaActivity.class);
            startActivity(descarga);
        }  else if (id == R.id.nav_inicio) {
            Intent mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);
        } else if (id == R.id.nav_replace) {
            startActivity(getIntent());
        }  else if(id == R.id.nav_cambio){
            Intent intent = new Intent(ReemplazarActivity.this,  CambioClaveActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setInfoCamion(String idCamion) {
        if (idCamion != "0") {
            camion = new Camion(getApplicationContext());
            camion = camion.find(Integer.valueOf(idCamion));
            infoCamion.setText(
                    Html.fromHtml(
                            "<h1>Camion:" + camion.economico  +
                                    "</h1><font color=\"#A0A0A0\">Placas: </font> " + camion.placas +
                                    "<br><font color=\"#A0A0A0\">Modelo: </font>" + camion.modelo +
                                    "<br><font color=\"#A0A0A0\">Ancho: </font>" + String.valueOf(camion.ancho) +
                                    "<br><font color=\"#A0A0A0\">Alto: </font>" + String.valueOf(camion.alto) +
                                    "<br><font color=\"#A0A0A0\">Largo: </font>" + String.valueOf(camion.largo) +
                            "<br><font color=\"#A0A0A0\">TAG (UID): </font>" + camion.tag.UID
                    )
            );
        } else {
            infoCamion.setText("");
        }
    }

    private void checkNfcEnabled() {
        Boolean nfcEnabled = adapter.isEnabled();
        if (!nfcEnabled) {
            new android.app.AlertDialog.Builder(ReemplazarActivity.this)
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

    private void WriteModeOn() {
        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);

        cambiarButton.setVisibility(View.GONE);
        infoCamion.setVisibility(View.GONE);
        camionesSpinner.setVisibility(View.GONE);
        mainTitle.setVisibility(View.GONE);

        fabCancel.setVisibility(View.VISIBLE);
        nfcImage.setVisibility(View.VISIBLE);

    }

    private void WriteModeOff() {
        writeMode = false;
        adapter.disableForegroundDispatch(this);

        infoCamion.setVisibility(View.VISIBLE);
        camionesSpinner.setVisibility(View.VISIBLE);
        mainTitle.setVisibility(View.VISIBLE);
        cambiarButton.setVisibility(View.VISIBLE);

        fabCancel.setVisibility(View.GONE);
        nfcImage.setVisibility(View.GONE);
    }

    public static String byteArrayToHexString(byte[] byteArray){
        return String.format("%0" + (byteArray.length * 2) + "X", new BigInteger(1,byteArray));
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        String mensaje;
        Integer contador = 0;
        int tipo = 0;
        String UID = "";
        Boolean result = false;
        if(writeMode) {
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                String[] techs = myTag.getTechList();
                for (String t : techs) {
                    if (MifareClassic.class.getName().equals(t)) {
                        nfc = new NFCTag(myTag, this);
                        UID = nfc.idTag(myTag);
                        tipo=1;
                    }
                    else if (MifareUltralight.class.getName().equals(t)) {
                        nfcUltra = new NFCUltralight(myTag, this);
                        UID = byteArrayToHexString(myTag.getId());
                        tipo=2;
                    }
                }

                if(TagModel.exists(UID, getApplicationContext())) {
                    if (TagModel.tagDisponible(UID, getApplicationContext())) {
                        contador = camion.getNumeroViajes(Integer.valueOf(idCamion));
                        if (tipo == 1) {
                            mensaje = nfc.concatenar(idCamion, User.getIdProyecto(getApplicationContext()));
                            nfc.formatear(myTag);
                            boolean limpiar = nfc.clean(myTag, 1);
                            System.out.println("LIMPIAR " + limpiar);
                            if (nfc.writeSector(myTag, 0, 1, mensaje) && nfc.writeSector(myTag, 2, 8, String.valueOf(contador))) {
                                boolean cambio = nfc.changeKey(myTag);
                                if (cambio == true) {
                                    Integer idcamionTAG = Util.getIdCamion(nfc.readSector(myTag, 0, 1));
                                    Integer contadorTAG = Integer.valueOf(nfc.readSector(myTag, 2, 8));

                                    if ((Integer.valueOf(idCamion).equals(Integer.valueOf(idcamionTAG))) && (Integer.valueOf(contador).equals(Integer.valueOf(contadorTAG)))) {
                                        result = true;
                                    } else {
                                        result = false;
                                    }
                                } else {
                                    Toast.makeText(ReemplazarActivity.this, getString(R.string.error_tag_comunicacion_KEY), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(ReemplazarActivity.this, getString(R.string.error_tag_comunicacion), Toast.LENGTH_LONG).show();
                            }
                        }
                        if (tipo == 2) {
                            nfcUltra.formateo(myTag);
                            mensaje = nfcUltra.concatenar(idCamion, User.getIdProyecto(this));
                            if (nfcUltra.writePagina(myTag, 4, mensaje) && nfcUltra.writeViaje(myTag, String.valueOf(contador))) {
                                System.out.println(Integer.valueOf(idCamion));
                                System.out.println(Integer.valueOf(nfcUltra.readPage(myTag, 4)));
                                System.out.println(contador);
                                System.out.println(Integer.valueOf(nfcUltra.readPage(myTag, 7)));
                                if ((Integer.valueOf(idCamion).equals(Integer.valueOf(nfcUltra.readPage(myTag, 4)))) && (contador.equals(Integer.valueOf(nfcUltra.readPage(myTag, 7))))) {
                                    result = true;
                                } else {
                                    result = false; // checar si es este el problema
                                }
                            } else {
                                Toast.makeText(ReemplazarActivity.this, getString(R.string.error_tag_comunicacion), Toast.LENGTH_LONG).show();
                            }
                        }

                        if (result) {
                            Toast.makeText(ReemplazarActivity.this, getString(R.string.tag_configurado), Toast.LENGTH_LONG).show();
                            TagModel.update(UID, idCamion, getApplicationContext(), true);
                            Intent mainActivity = new Intent(ReemplazarActivity.this, MainActivity.class);
                            startActivity(mainActivity);
                        } else {
                            Toast.makeText(ReemplazarActivity.this, "No se pudo configurar el TAG, por favor intentelo de nuevo.", Toast.LENGTH_LONG).show();

                        }
                    }else {
                        String camion = TagModel.findCamion(UID, getApplicationContext());
                        if(camion==null){
                            camion = TagModel.findDisponibleCamion(UID,getApplicationContext());
                        }
                        Toast.makeText(ReemplazarActivity.this, getString(R.string.error_tag_configurado)+camion, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ReemplazarActivity.this, getString(R.string.error_tag_inexistente), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

}
