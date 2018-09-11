package fm.feed.androidsdk2.richplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

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
    @BindView(R.id.open_offline)
    Button stationsOffline;
    @BindView(R.id.open_remote_offline)
    Button stationsRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);

        FeedPlayerService.getInstance(new FeedAudioPlayer.AvailabilityListener() {
            @Override
            public void onPlayerAvailable(FeedAudioPlayer feedAudioPlayer) {
                if(feedAudioPlayer.getRemoteStationList().size() > 0)
                {
                    stationsRemote.setEnabled(true);
                }
            }

            @Override
            public void onPlayerUnavailable(Exception e) {

            }
        });

        FeedPlayerService.getInstance(new FeedAudioPlayer.OfflineAvailabilityListener() {
            @Override
            public void onOfflineStationsAvailable(FeedAudioPlayer feedAudioPlayer) {
                stationsOffline.setEnabled(true);
            }

            @Override
            public void offlineMusicUnAvailable() {

            }
        });
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
    public void OpenOnDemandStation()
    {
        Intent ai = new Intent(this, MainActivity.class);
        ai.putExtra("Target","OnDemand");
        startActivity(ai);
    }

    @OnClick(R.id.open_station)
    public void OpenStationsView()
    {
        Intent ai = new Intent(this, MainActivity.class);
        ai.putExtra("Target","Online");
        startActivity(ai);
    }
}
