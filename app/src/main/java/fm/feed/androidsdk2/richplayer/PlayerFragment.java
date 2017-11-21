package fm.feed.androidsdk2.richplayer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedException;
import fm.feed.android.playersdk.FeedPlayerService;
import fm.feed.android.playersdk.models.AudioFile;
import fm.feed.android.playersdk.models.Play;
import fm.feed.android.playersdk.models.Station;


public class PlayerFragment extends Fragment  {



    private static final String TAG = "PLAYER_FRAGMENT";
    private static final String DEFAULT_STATION_ID = "station_id";
    FeedAudioPlayer mPlayer;
    Context mContext;
    private int mStationID;
    Station mStation;
    FragmentManager mFragmentManager;

    @BindView(R.id.powered_by_feed)   TextView textView;
    @BindView(R.id.title_track)       TextView trackText;
    @BindView(R.id.artist_track)      TextView ArtistText;
    @BindView(R.id.likeButton)        ImageButton likeButton;
    @BindView(R.id.dislikeButton)     ImageButton dislikeButton;
    @BindView(R.id.play_pause_button) ImageButton playPauseButton;
    @BindView(R.id.skipButton)        ImageButton skipButton;
    @BindView(R.id.buffering_spinner) ProgressBar bufferingBar;
    @BindView(R.id.historyButton)     ImageButton playHistory;
    @BindView(R.id.toolbar)           Toolbar toolbar;
    @BindView(R.id.back_button)       ImageButton backButton;
    @BindView(R.id.toolbar_title)     TextView toolbarTitle;
    @BindView(R.id.onDemandButton)    ImageButton onDemandButton;

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
        if (mPlayer.getState() != FeedAudioPlayer.State.PLAYING) {
            mPlayer.play();
        } else if (mPlayer.getState() == FeedAudioPlayer.State.PLAYING) {
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
        mListener.onClickPoweredBy();
    }


    @OnClick(R.id.historyButton)
    public void showHistory()
    {
            FragmentManager manager = getChildFragmentManager();
            PlayHistoryFragment fragment = new PlayHistoryFragment();
            //OnDemandFragment fragment = new OnDemandFragment();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down);
            transaction.addToBackStack(StationsFragment.class.getSimpleName());
            transaction.replace(R.id.container_view, fragment).commit();
    }

    @OnClick(R.id.onDemandButton)
    public void onDemandButton()
    {
        mListener.OnDemandButtonClicked(mStationID);
    }
    /**
     * When we change stations or run out of music, update
     * the displayed metadata.
     */

    FeedAudioPlayer.StateListener stateListener = new FeedAudioPlayer.StateListener() {
        @Override
        public void onStateChanged(FeedAudioPlayer.State state) {

            if(state == FeedAudioPlayer.State.PLAYING)
            {
                playPauseButton.setVisibility(View.VISIBLE);
                bufferingBar.setVisibility(View.INVISIBLE);
                playPauseButton.setImageResource(R.drawable.pause_black);
            }
            else if(state == FeedAudioPlayer.State.PAUSED)
            {
                playPauseButton.setVisibility(View.VISIBLE);
                bufferingBar.setVisibility(View.INVISIBLE);
                playPauseButton.setImageResource(R.drawable.play_black);
            }
            else if(state == FeedAudioPlayer.State.STALLED)
            {
                playPauseButton.setVisibility(View.INVISIBLE);
                bufferingBar.setVisibility(View.VISIBLE);
            }

        }
    };

    FeedAudioPlayer.StationChangedListener stationChangedListener = new FeedAudioPlayer.StationChangedListener() {
        @Override
        public void onStationChanged(Station station) {
            if(station.getAudioFiles() != null) {
                onDemandButton.setVisibility(View.VISIBLE);
            }
            else {
                onDemandButton.setVisibility(View.GONE);
            }
            mStation = station;
            mStationID = station.getId();
        }
    };

    FeedAudioPlayer.PlayListener playListener = new FeedAudioPlayer.PlayListener() {

        @Override
        public void onSkipStatusChanged(boolean b) {
            skipButton.setEnabled(b);
        }

        @Override
        public void onProgressUpdate(Play play, float elapsedTime, float duration) {

        }

        @Override
        public void onPlayStarted(Play play) {
            trackText.setText(play.getAudioFile().getTrack().getTitle());
            ArtistText.setText(play.getAudioFile().getArtist().getName());
            likeStatusChangeListener.onLikeStatusChanged(play.getAudioFile());

        }
    };

    FeedAudioPlayer.LikeStatusChangeListener likeStatusChangeListener = new FeedAudioPlayer.LikeStatusChangeListener() {
        @Override
        public void onLikeStatusChanged(AudioFile audioFile) {
            if (audioFile.isDisliked()) {
                dislikeButton.setImageResource(R.drawable.ic_dislike_filled_black);
            } else if (audioFile.isLiked()) {
                likeButton.setImageResource(R.drawable.ic_like_filled_black);
            } else {
                likeButton.setImageResource(R.drawable.ic_like_unfilled_black);
                dislikeButton.setImageResource(R.drawable.ic_dislike_unfilled_black);
            }
        }
    };

    FeedAudioPlayer.UnhandledErrorListener errorListener = new FeedAudioPlayer.UnhandledErrorListener() {
        @Override
        public void onUnhandledError(FeedException e) {
            Log.e(TAG, e.toString());
        }
    };

    // Station has ended, so we are switching to next station.
    FeedAudioPlayer.OutOfMusicListener outOfMusicListener = new FeedAudioPlayer.OutOfMusicListener() {
        @Override
        public void onOutOfMusic() {
            int inx = mPlayer.getStationList().indexOf(mStation);
            if(mPlayer.getStationList().size() >= inx+1)  // If we have more stations available switch to them
            {
                mPlayer.setActiveStation(mPlayer.getStationList().get(inx+1));
            }
        }
    };

    FeedAudioPlayer.SkipListener skipListener = new FeedAudioPlayer.SkipListener() {
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


    public static PlayerFragment newInstance(int stationId) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(DEFAULT_STATION_ID, stationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStationID = getArguments().getInt(DEFAULT_STATION_ID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, view);

        FeedPlayerService.getInstance(new FeedAudioPlayer.AvailabilityListener() {
            @Override
            public void onPlayerAvailable(FeedAudioPlayer feedAudioPlayer) {
                mPlayer = feedAudioPlayer;
                mPlayer.addPlayListener(playListener);
                mPlayer.addStationChangedListener(stationChangedListener);
                mPlayer.addStateListener(stateListener);
                mPlayer.addLikeStatusChangeListener(likeStatusChangeListener);
                mPlayer.addUnhandledErrorListener(errorListener);
                mPlayer.addOutOfMusicListener(outOfMusicListener);
                mPlayer.addSkipListener(skipListener);
                mStation = MainActivity.getStationById(mStationID, mPlayer.getStationList());
                if(mStation!=null && mStation.getAudioFiles() != null) {
                    onDemandButton.setVisibility(View.VISIBLE);
                }
                else {
                    onDemandButton.setVisibility(View.GONE);
                }
                stateListener.onStateChanged(feedAudioPlayer.getState());
            }

            @Override
            public void onPlayerUnavailable(Exception e) {
                Toast.makeText(mContext, "Unexpected error", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null)
                {
                    getActivity().onBackPressed();
                }
            }
        });
        toolbarTitle.setText("Now Playing");
        mFragmentManager = getChildFragmentManager();
        AlbumArtFragment fragment = AlbumArtFragment.newInstance(mStationID);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down, R.anim.slide_in_up, R.anim.slide_out_down);
        transaction.add(R.id.container_view,fragment, AlbumArtFragment.class.getSimpleName()).commitNow();
        return view;
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
        mContext = null;
    }

    public interface OnPlayerFragmentInteractionListener {
        void onClickPoweredBy();
        void OnDemandButtonClicked(int newStationID);
    }
}
