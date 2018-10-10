package fm.feed.androidsdk2.richplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.Result;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class LaunchActivity extends AppCompatActivity {

    @BindView(R.id.open_onDemand)
    Button onDemandButton;
    @BindView(R.id.open_station)
    Button stationsButton;
    @BindView(R.id.scan_token)
    Button scanButtion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.open_onDemand)
    public void OpenOnDemandStation() {
        Intent ai = new Intent(this, MainActivity.class);
        ai.putExtra("Target", "OnDemand");
        startActivity(ai);
    }

    @OnClick(R.id.open_station)
    public void OpenStationsView() {
        Intent ai = new Intent(this, MainActivity.class);
        startActivity(ai);
    }

    @OnClick(R.id.scan_token)
    public void ScanTokens() {

        ActivityCompat.requestPermissions(LaunchActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent ai = new Intent(this, QrreaderActivity.class);
                    startActivity(ai);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(LaunchActivity.this, "Permission denied to start camera", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}


