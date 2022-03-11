package fm.feed.androidsdk2.richplayer;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fm.feed.android.playersdk.AvailabilityListener;
import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedPlayerService;
import fm.feed.android.playersdk.StationChangedListener;
import fm.feed.android.playersdk.models.NotificationStyle;
import fm.feed.android.playersdk.models.Station;

public class MainActivity extends AppCompatActivity implements StationsFragment.StationSelectionListener, PlayerFragment.OnPlayerFragmentInteractionListener{


    @BindView(R.id.tv_unavailable)
    TextView tvUnavailable;
    @BindView(R.id.loading_player)
    ProgressBar progressBar;
    @BindView(R.id.toolbar)
    androidx.appcompat.widget.Toolbar toolbar;
    FragmentManager mFragmentManager;
    private FeedAudioPlayer feedAudioPlayer;
    private List<Station> stationList;
    private boolean isOfflineMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if ((ab != null) && (getSupportParentActivityIntent() != null)) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        mFragmentManager = getSupportFragmentManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String str  = intent.getStringExtra("Target");
        feedAudioPlayer = FeedPlayerService.getInstance();
        if(str.equals("Offline")) {
            isOfflineMode = true;
            stationList = feedAudioPlayer.getLocalOfflineStationList();
            setupPlayer();
        }
        else if(str.equals("Online"))  {
            isOfflineMode = false;
            feedAudioPlayer.addAvailabilityListener(new AvailabilityListener() {
                @Override
                public void onPlayerAvailable(FeedAudioPlayer feedAudioPlayer) {
                    stationList = feedAudioPlayer.getStationList();
                    setupPlayer();
                }

                @Override
                public void onPlayerUnavailable(Exception e) {
                    tvUnavailable.setVisibility(View.VISIBLE);
                }
            });

        }

    }

    void setupPlayer()
    {
        // Only needed for below api 26, does not work above 26
        final NotificationStyle ni = new NotificationStyle()
                .setSmallIcon(R.drawable.play_white_15dp)
                .setPlayIcon(R.drawable.play_white_15dp)
                .setPauseIcon(R.drawable.pause_white_15dp)
                .setSkipIcon(R.drawable.skip_white_15dp)
                .setColor(Color.BLACK)
                .setThumbsUpIcons(R.drawable.like_unfilled_white_15pt, R.drawable.like_filled_white_15pt)
                .setThumbsDownIcons(R.drawable.dislike_unfilled_white_15pt, R.drawable.dislike_filled_white_15pt)

                // .. and our custom notification layouts
                .setBigContentView(getPackageName(), R.layout.notification_small)
                .setContentView(getPackageName(), R.layout.notification_small)
                .setMediaImageId(R.id.notification_icon)
                .setProgressId(R.id.progress)
                .setDislikeButtonId(R.id.dislike_button)
                .setLikeButtonId(R.id.like_button)
                .setPlayPauseButtonId(R.id.play_pause_button)
                .setSkipButtonId(R.id.skip_button)
                .setTrackTextId(R.id.notification_track_title)
                .setArtistTextId(R.id.notification_track_artist)
                .setReleaseTextId(R.id.notification_track_release);

        mFragmentManager = getSupportFragmentManager();

        FeedPlayerService.getInstance(new AvailabilityListener() {
            @Override
            public void onPlayerAvailable(FeedAudioPlayer aFeedAudioPlayer) {
                feedAudioPlayer = aFeedAudioPlayer;
                feedAudioPlayer.addStationChangedListener(stationListener);
                feedAudioPlayer.setMaxBitrate(0);
                stationListener.onStationChanged(feedAudioPlayer.getActiveStation());
                progressBar.setVisibility(View.INVISIBLE);
                feedAudioPlayer.setNotificationStyle(ni);
                loadStationsFragment();
                setPendingIntent();
            }

            @Override
            public void onPlayerUnavailable(Exception e) {
                tvUnavailable.setVisibility(View.VISIBLE);
            }
        });
        feedAudioPlayer.addStationChangedListener(stationListener);
        stationListener.onStationChanged(feedAudioPlayer.getActiveStation());
        progressBar.setVisibility(View.INVISIBLE);
        feedAudioPlayer.setNotificationStyle(ni);
        loadStationsFragment();
        setPendingIntent();
    }

    StationChangedListener stationListener  = new StationChangedListener() {
        @Override
        public void onStationChanged(@NotNull Station station) {

        }
    };

    private void setPendingIntent(){
        Intent ai = new Intent(getIntent());
        ai.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, ai, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        feedAudioPlayer.setPendingIntent(pi);
    }

    private void loadStationsFragment()
    {
        StationsFragment fragment = StationsFragment.newInstance(isOfflineMode);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down, R.anim.slide_in_up,R.anim.slide_out_down);
        transaction.replace(R.id.baseLayout, fragment).commit();
    }

    private void loadPlayerFragment(int stationId)
    {
        PlayerFragment fragment = PlayerFragment.newInstance(stationId, isOfflineMode);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down, R.anim.slide_in_up,R.anim.slide_out_down);
        transaction.addToBackStack(PlayerFragment.class.getSimpleName());
        transaction.replace(R.id.baseLayout, fragment).commit();
    }

    @Override
    public void OnDemandButtonClicked(int newStationID) {

        OnDemandFragment fragment = new OnDemandFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down);
        transaction.addToBackStack(StationsFragment.class.getSimpleName());
        transaction.replace(R.id.baseLayout, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        for (Fragment frag : fm.getFragments()) {
            if (frag.isVisible()) {
                FragmentManager childFm = frag.getChildFragmentManager();
                if (childFm.getBackStackEntryCount() > 0) {
                    for (Fragment childfragnested: childFm.getFragments()) {
                        FragmentManager childFmNestManager = childfragnested.getFragmentManager();
                        if(childfragnested.isVisible() && childFmNestManager !=null) {
                            childFmNestManager.popBackStack();
                            return;
                        }
                    }
                }
            }
        }
        super.onBackPressed();

    }

    @Override
    public void onStationSelected(long stationId) {

        Station station = getStationById((int)stationId, stationList);
        if(station != null)
        {
            feedAudioPlayer.setActiveStation(station,false);
            loadPlayerFragment((int)stationId);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return true;
    }

    public static @Nullable Station getStationById(int id, List<Station> stationList)
    {
        Station tStation = null;
        for (Station station: stationList)
        {
            if(station.getId() == id)
            {
                tStation = station; break;
            }
        }
        return tStation;
    }
}
