package mx.grupohi.acarreostag;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SyncActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_sync_activity);
        setContentView(R.layout.activity_sync);
    }
}
