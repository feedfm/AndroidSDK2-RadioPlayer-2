package fm.feed.androidsdk2.richplayer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedPlayerService;

public class LaunchActivity extends AppCompatActivity {

    @BindView(R.id.open_onDemand)
    Button onDemandButton;
    @BindView(R.id.open_station)
    Button stationsButton;
    @BindView(R.id.scan_token)
    Button scanButtion;


    @BindView(R.id.open_offline)
    Button stationsOffline;
    @BindView(R.id.open_remote_offline)
    Button stationsRemote;

    @BindView(R.id.switch1)
    Switch internationalSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);
        FeedAudioPlayer player = FeedPlayerService.getInstance();
        internationalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    SharedPreferences settings;
                    settings = getSharedPreferences("FEEDCREDS", MODE_PRIVATE);
                    String token = settings.getString("token", "offline");
                    String secret = settings.getString("secret", "offline");
                    FeedAudioPlayer.Builder builder = new FeedAudioPlayer.Builder()
                            .setSecret(secret)
                            .setToken(token)
                            .setContext(getApplicationContext())
                            .setMockLocation(FeedAudioPlayer.MockLocations.EU);
                    FeedPlayerService.initialize(builder);
                }
                else {

                    SharedPreferences settings;
                    settings = getSharedPreferences("FEEDCREDS", MODE_PRIVATE);
                    String token = settings.getString("token", "offline");
                    String secret = settings.getString("secret", "offline");
                    FeedAudioPlayer.Builder builder = new FeedAudioPlayer.Builder()
                            .setSecret(secret)
                            .setToken(token)
                            .setContext(getApplicationContext());
                    FeedPlayerService.initialize(builder);
                }
            }
        });
        player.addAvailabilityListener(new FeedAudioPlayer.AvailabilityListener() {
            @Override
            public void onPlayerAvailable(FeedAudioPlayer feedAudioPlayer) {
                if(feedAudioPlayer.getRemoteOfflineStationList().size() > 0)
                {
                    stationsRemote.setEnabled(true);
                }
            }

            @Override
            public void onPlayerUnavailable(Exception e) {

            }
        });

        if (player.getLocalOfflineStationList().size() > 0){
            stationsOffline.setEnabled(true);
        }
    }

    @OnClick(R.id.open_offline)
    public void StationsOffline() {
        Intent ai = new Intent(this, MainActivity.class);
        ai.putExtra("Target","Offline");
        startActivity(ai);
    }

    @OnClick(R.id.open_remote_offline)
    public void OpenRemoteOffline() {
        Intent ai = new Intent(this, RemoteList.class);
        startActivity(ai);

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
        ai.putExtra("Target","Online");
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


