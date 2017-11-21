package fm.feed.androidsdk2.richplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedPlayerService;
import fm.feed.android.playersdk.models.Station;

public class MainActivity extends AppCompatActivity implements StationsFragment.StationSelectionListener, PlayerFragment.OnPlayerFragmentInteractionListener{

    @BindView(R.id.tv_unavailable)
    TextView tvUnavailable;
    @BindView(R.id.loading_player)
    ProgressBar progressBar;
    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.back_button)
    ImageButton backButton;
    FragmentManager mFragmentManager;
    FeedAudioPlayer feedAudioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mFragmentManager = getSupportFragmentManager();
        FeedPlayerService.getInstance(new FeedAudioPlayer.AvailabilityListener() {
            @Override
            public void onPlayerAvailable(FeedAudioPlayer aFeedAudioPlayer) {
                feedAudioPlayer = aFeedAudioPlayer;
                progressBar.setVisibility(View.INVISIBLE);
                loadStationsFragment();
            }

            @Override
            public void onPlayerUnavailable(Exception e) {
                tvUnavailable.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadStationsFragment()
    {
        StationsFragment fragment = new StationsFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.baseLayout, fragment).commit();
    }

    private void loadPlayerFragment(int stationId)
    {
        PlayerFragment fragment = PlayerFragment.newInstance(stationId);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.addToBackStack(StationsFragment.class.getSimpleName());
        transaction.replace(R.id.baseLayout, fragment).commit();
    }


   /* @Override
    public void onStationChanged(int newStationID) {
        Station station = getStationById((int)newStationID, feedAudioPlayer.getStationList());
        if(station != null) {
            feedAudioPlayer.setActiveStation(station);
            feedAudioPlayer.prepareToPlay(null);
        }
    }*/

    @Override
    public void onClickPoweredBy() {
        Intent ai = new Intent(this, PoweredByFeedActivity.class);
        startActivity(ai);
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
                        if(childfragnested.isVisible()) {
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

        Station station = getStationById((int)stationId, feedAudioPlayer.getStationList());
        if(station != null)
        {
            feedAudioPlayer.setActiveStation(station);
            feedAudioPlayer.prepareToPlay(null);
            loadPlayerFragment((int)stationId);
            assignLockScreen(station);
        }
    }

    private void assignLockScreen(Station station) {
        String bgUrl;
        try {
            bgUrl = (String) station.getOption("background_image_url");
        } catch (ClassCastException e) {
            bgUrl = null;
        }

        if (bgUrl != null) {

            Glide.with(this).load(bgUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    // set this image for the lockscreen and notifications if we're
                    // playing this station
                    feedAudioPlayer.setArtwork(resource);
                }
            });
        } else {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_station_background);
            feedAudioPlayer.setArtwork(bm);
        }
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
