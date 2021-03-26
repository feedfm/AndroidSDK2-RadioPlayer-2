package fm.feed.androidsdk2.richplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fm.feed.android.playersdk.AvailabilityListener;
import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedException;
import fm.feed.android.playersdk.FeedPlayerService;
import fm.feed.android.playersdk.LikeStatusChangeListener;
import fm.feed.android.playersdk.OutOfMusicListener;
import fm.feed.android.playersdk.PlayListener;
import fm.feed.android.playersdk.SkipListener;
import fm.feed.android.playersdk.State;
import fm.feed.android.playersdk.StateListener;
import fm.feed.android.playersdk.StationChangedListener;
import fm.feed.android.playersdk.UnhandledErrorListener;
import fm.feed.android.playersdk.models.AudioFile;
import fm.feed.android.playersdk.models.Play;
import fm.feed.android.playersdk.models.Station;


public class PlayerFragment extends Fragment  {



    private static final String TAG = "PLAYER_FRAGMENT";
    private static final String DEFAULT_STATION_ID = "station_id";
    private static final String IS_MODE_OFFLINE = "isModeOffline";
    FeedAudioPlayer mPlayer;
    Context mContext;
    private int mStationID;
    Station mStation;
    boolean isNew = false;
    FragmentManager mFragmentManager;
    List<Station> localStationList;

    @BindView(R.id.powered_by_feed)   TextView textView;
    @BindView(R.id.title_track)       TextView trackText;
    @BindView(R.id.artist_track)      TextView ArtistText;
    @BindView(R.id.likeButton)        ImageButton likeButton;
    @BindView(R.id.dislikeButton)     ImageButton dislikeButton;
    @BindView(R.id.play_pause_button) ImageButton playPauseButton;
    @BindView(R.id.skipButton)        ImageButton skipButton;
    @BindView(R.id.buffering_spinner) ProgressBar bufferingBar;
    @BindView(R.id.historyButton)     ImageButton playHistory;
    @BindView(R.id.onDemandButton)    ImageButton onDemandButton;
    private boolean isOfflineMode;


    @OnClick(R.id.likeButton)
    public void likeButtonClick() {
        if(mPlayer.getCurrentPlay() != null) {
            if (!mPlayer.getCurrentPlay().getAudioFile().isLiked()) {
                mPlayer.like();
            } else {
                mPlayer.unlike();
            }
        }
    }

    @OnClick(R.id.dislikeButton)
    public void dislikeButtonClick() {
        if(mPlayer.getCurrentPlay() != null) {
            if (!mPlayer.getCurrentPlay().getAudioFile().isDisliked()) {
                mPlayer.dislike();
            } else {
                mPlayer.unlike();
            }
        }
    }

    @OnClick(R.id.play_pause_button)
    public void playPause()
    {
        if (mPlayer.getState() != State.PLAYING) {
            mPlayer.play();
        } else if (mPlayer.getState() == State.PLAYING) {
            mPlayer.pause();
        }
    }

    @OnClick(R.id.skipButton)
    public void skipButton()
    {
        mPlayer.skip();
        bufferingBar.setVisibility(View.VISIBLE);
        playPauseButton.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.powered_by_feed)
    public void poweredByFeed()
    {
        Intent ai = new Intent(mContext, PoweredByFeedActivity.class);
        startActivity(ai);
    }

    @OnClick(R.id.historyButton)
    public void showHistory()
    {
        FragmentManager manager = getChildFragmentManager();
        if(manager.getBackStackEntryCount() == 0) {
            PlayHistoryFragment fragment = PlayHistoryFragment.newInstance(isOfflineMode);
            //OnDemandFragment fragment = new OnDemandFragment();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down,R.anim.slide_in_up, R.anim.slide_out_down );
            transaction.addToBackStack(StationsFragment.class.getSimpleName());
            transaction.replace(R.id.container_view, fragment).commit();
        }
        else {
            manager.popBackStack();
        }
    }

    @OnClick(R.id.onDemandButton)
    public void onDemandButton()
    {
        Timer timer = new Timer();
        timer.schedule(
                new TimerTask() {
            @Override
            public void run() {
                mListener.OnDemandButtonClicked(mStationID);
            }
        }, 500);


    }
    /**
     * When we change stations or run out of music, update
     * the displayed metadata.
     */

    StateListener stateListener = new StateListener() {
        @Override
        public void onStateChanged(State state) {

        if(state == State.PLAYING)
        {
            playPauseButton.setVisibility(View.VISIBLE);
            bufferingBar.setVisibility(View.INVISIBLE);
            playPauseButton.setImageResource(R.drawable.pause_black);
        }
        else if(state == State.PAUSED)
        {
            playPauseButton.setVisibility(View.VISIBLE);
            bufferingBar.setVisibility(View.INVISIBLE);
            playPauseButton.setImageResource(R.drawable.play_black);
        }
        else if(state == State.STALLED)
        {
            ((Activity)mContext).runOnUiThread(() -> {

                playPauseButton.setVisibility(View.INVISIBLE);
                bufferingBar.setVisibility(View.VISIBLE);
            });
        }

        }
    };

    StationChangedListener stationChangedListener = new StationChangedListener() {
        @Override
        public void onStationChanged(Station station) {
            if(station.getAudioFiles() != null) {
                onDemandButton.setVisibility(View.VISIBLE);
                playHistory.setVisibility(View.GONE);
            }
            else {
                onDemandButton.setVisibility(View.GONE);
                playHistory.setVisibility(View.VISIBLE);
            }
            mStation = station;
            mStationID = station.getId();
        }
    };

    PlayListener playListener = new PlayListener() {

        @Override
        public void onSkipStatusChanged(boolean b) {

        }

        @Override
        public void onProgressUpdate(Play play, float elapsedTime, float duration) {

            if(trackText.getText().equals("")){
                trackText.setText(play.getAudioFile().getTrack().getTitle());
                ArtistText.setText(play.getAudioFile().getArtist().getName());
                likeStatusChangeListener.onLikeStatusChanged(play.getAudioFile());
            }
        }

        @Override
        public void onPlayStarted(Play play) {
            if(play != null) {
                trackText.setText(play.getAudioFile().getTrack().getTitle());
                ArtistText.setText(play.getAudioFile().getArtist().getName());
                likeStatusChangeListener.onLikeStatusChanged(play.getAudioFile());
            }

        }
    };

    LikeStatusChangeListener likeStatusChangeListener = new LikeStatusChangeListener() {
        @Override
        public void onLikeStatusChanged(AudioFile audioFile) {
            if((mPlayer.getCurrentPlay() != null && audioFile.getId().equals(mPlayer.getCurrentPlay().getAudioFile().getId()))) {
                    if (audioFile.isDisliked()) {
                        dislikeButton.setImageResource(R.drawable.dislike_filled_black);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            likeButton.setImageTintMode(PorterDuff.Mode.MULTIPLY);
                            dislikeButton.setImageTintMode(PorterDuff.Mode.MULTIPLY);
                        }
                    } else if (audioFile.isLiked()) {
                        likeButton.setImageResource(R.drawable.like_filled_black);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            likeButton.setImageTintMode(PorterDuff.Mode.MULTIPLY);
                            dislikeButton.setImageTintMode(PorterDuff.Mode.MULTIPLY);
                        }
                    } else {
                        likeButton.setImageResource(R.drawable.like_unfilled_black);
                        dislikeButton.setImageResource(R.drawable.dislike_unfilled_black);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            likeButton.setImageTintMode(PorterDuff.Mode.SRC_IN);
                            dislikeButton.setImageTintMode(PorterDuff.Mode.SRC_IN);
                        }
                    }
            }
        }
    };

    UnhandledErrorListener errorListener = new UnhandledErrorListener() {
        @Override
        public void onUnhandledError(FeedException e) {
            Log.e(TAG, e.toString());
        }
    };

    // Station has ended, so we are switching to a next station.
    OutOfMusicListener outOfMusicListener = new OutOfMusicListener() {
        @Override
        public void onOutOfMusic() {
            int inx = localStationList.indexOf(mStation);
            if(localStationList.size() > inx+1)  // If we have more stations available switch to them
            {
                // TODO fix crash here
                mPlayer.setActiveStation(localStationList.get(inx+1), true);
            }
        }
    };

    SkipListener skipListener = new SkipListener() {
        @Override
        public void requestCompleted(boolean b) {

            playPauseButton.setVisibility(View.VISIBLE);
            bufferingBar.setVisibility(View.INVISIBLE);
            if(!b)
            {
                Toast.makeText(mContext, "Skip Limit reached!" , Toast.LENGTH_SHORT).show();
            }
        }
    };

    private OnPlayerFragmentInteractionListener mListener;

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance(int stationId, boolean isOfflineMode) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(DEFAULT_STATION_ID, stationId);
        args.putBoolean(IS_MODE_OFFLINE, isOfflineMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStationID = getArguments().getInt(DEFAULT_STATION_ID);
            isOfflineMode = getArguments().getBoolean(IS_MODE_OFFLINE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, view);
        setRetainInstance(true);
        if(getActivity() != null && ((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Now playing");
        }
        mPlayer = FeedPlayerService.getInstance();
        if(!isOfflineMode) {

            mPlayer.addAvailabilityListener(new AvailabilityListener() {
                @Override
                public void onPlayerAvailable(FeedAudioPlayer feedAudioPlayer) {
                    localStationList = mPlayer.getStationList();
                    mPlayer = feedAudioPlayer;
                    setupPlayer(savedInstanceState);
                }

                @Override
                public void onPlayerUnavailable(Exception e) {
                    Toast.makeText(mContext, "Unexpected error", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            if (mPlayer.getLocalOfflineStationList().size()>0)
            {
                if ((getActivity() != null)) {
                    localStationList = mPlayer.getLocalOfflineStationList();
                }
                setupPlayer(savedInstanceState);
            }
        }

        mFragmentManager = getChildFragmentManager();
        AlbumArtFragment fragment = AlbumArtFragment.newInstance(mStationID, isOfflineMode);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down, R.anim.slide_in_up, R.anim.slide_out_down);
        transaction.add(R.id.container_view,fragment, AlbumArtFragment.class.getSimpleName()).commitNow();
        return view;
    }


    public void setupPlayer(Bundle savedInstanceState){
        mPlayer.addPlayListener(playListener);
        mPlayer.addStationChangedListener(stationChangedListener);
        mPlayer.addStateListener(stateListener);
        mPlayer.addLikeStatusChangeListener(likeStatusChangeListener);
        mPlayer.addUnhandledErrorListener(errorListener);
        mPlayer.addOutOfMusicListener(outOfMusicListener);
        mPlayer.addSkipListener(skipListener);
        playHistory.setImageAlpha(204);
        onDemandButton.setImageAlpha(204);
        mStation = MainActivity.getStationById(mStationID, localStationList);
        if(mStation!=null && mStation.getAudioFiles() != null) {
            if (savedInstanceState == null && !isNew) {
                isNew = true;
                Timer timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                mListener.OnDemandButtonClicked(mStationID);
                            }
                        }, 1000);

                // Do this code only first time, not after rotation or reuse fragment from backstack
            }

            onDemandButton.setVisibility(View.VISIBLE);
            playHistory.setVisibility(View.GONE);
        }
        else {
            isNew = true;
            mPlayer.play();
            onDemandButton.setVisibility(View.GONE);
            playHistory.setVisibility(View.VISIBLE);
        }
        stateListener.onStateChanged(mPlayer.getState());
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof OnPlayerFragmentInteractionListener) {
            mListener = (OnPlayerFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnPlayerFragmentInteractionListener {
        void OnDemandButtonClicked(int newStationID);
    }
}
