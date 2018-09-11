package fm.feed.androidsdk2.richplayer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedPlayerService;
import fm.feed.android.playersdk.models.Play;
import fm.feed.android.playersdk.models.Station;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumArtFragment extends Fragment {


    private static final String TAG = AlbumArtFragment.class.getSimpleName();
    private static final String DEFAULT_STATION_ID = "station_id";
    FeedAudioPlayer mPlayer;
    Context mContext;
    private int mStationID;
    int selectedIndex = 0;
    int playingStationIndex;
    List<Station> localStationList;
    MyPagerAdapter pagerAdapter = null;

    @BindView(R.id.station_description_player) TextView stationDescription;
    @BindView(R.id.albumArtFlipper)     ViewPager viewPager;
    @BindView(R.id.progressBar)         ProgressBar mProgressBar;
    @BindView(R.id.station_name_player) TextView stationTitle;
    @BindView(R.id.nextStation)         ImageButton nextStation;
    @BindView(R.id.previousStation)     ImageButton previousStation;
    @BindView(R.id.shareButton)         ImageButton shareButton;
    @BindView(R.id.viewStations)        ImageButton viewStations;
    @BindView(R.id.playArtStation)      ImageButton playButton;
    @BindView(R.id.gradient_view)       ImageView gradientView;

    public AlbumArtFragment() {
        // Required empty public constructor
    }

    public static AlbumArtFragment newInstance(int stationId) {
        AlbumArtFragment fragment = new AlbumArtFragment();
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

    FeedAudioPlayer.PlayListener playListener = new FeedAudioPlayer.PlayListener() {
        @Override
        public void onSkipStatusChanged(boolean b) {

        }
        @Override
        public void onProgressUpdate(Play play, float elapsedTime, float duration) {

            mProgressBar.setMax((int) (duration * 1000));
            mProgressBar.setProgress((int) elapsedTime * 1000);
        }
        @Override
        public void onPlayStarted(Play play) {

        }
    };

    @OnClick(R.id.playArtStation)
    public void ChangeStation(View view)
    {
        playingStationIndex = selectedIndex;
        mPlayer.setActiveStation(localStationList.get(selectedIndex), false);
        //mPlayer.prepareToPlay(null);
        mPlayer.play();
        view.setVisibility(View.INVISIBLE);
        gradientView.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.shareButton)
    public void shareButton()
    {

    }

    @OnClick(R.id.nextStation)
    public void nextStation()
    {
            if(pagerAdapter.getCount() > selectedIndex) {
                viewPager.setCurrentItem(++selectedIndex);
            }

    }

    @OnClick(R.id.previousStation)
    public void previousStation()
    {
        if(selectedIndex > 0) {

            viewPager.setCurrentItem(--selectedIndex);
        }
    }

    @OnClick(R.id.viewStations)
    public void viewStation(){

        if(getActivity() !=null)
        {
            getActivity().onBackPressed();
        }
    }

    FeedAudioPlayer.StationChangedListener stationChangedListener = new FeedAudioPlayer.StationChangedListener() {
        @Override
        public void onStationChanged(Station station) {

            mStationID = station.getId();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_album_art, container, false);
        ButterKnife.bind(this, view);

        if((getActivity() != null) && !((MainActivity)getActivity()).isOfflineMode()) {
            FeedPlayerService.getInstance(new FeedAudioPlayer.AvailabilityListener() {
                @Override
                public void onPlayerAvailable(FeedAudioPlayer feedAudioPlayer) {
                    mPlayer = feedAudioPlayer;
                    if ((getActivity() != null)) {
                        localStationList = ((MainActivity) getActivity()).getStationList();
                    }
                    setUI();
                }

                @Override
                public void onPlayerUnavailable(Exception e) {

                }
            });
        }
        else {
            FeedPlayerService.getInstance(new FeedAudioPlayer.OfflineAvailabilityListener() {
                @Override
                public void onOfflineStationsAvailable(FeedAudioPlayer feedAudioPlayer) {
                    mPlayer = feedAudioPlayer;
                    if ((getActivity() != null)) {
                        localStationList = ((MainActivity) getActivity()).getStationList();
                    }
                    setUI();
                }

                @Override
                public void offlineMusicUnAvailable() {

                }
            });
        }

        return view;
    }

    public void setUI() {

        if(mPlayer!= null) {
            mPlayer.addPlayListener(playListener);
            mPlayer.addStationChangedListener(stationChangedListener);
        }
        if(mPlayer!= null && stationTitle !=null) {

            pagerAdapter = new MyPagerAdapter();
            int temp = 0;
            for (Station st: localStationList) {
                if(st.getId() == mStationID) {
                    break;
                }
                temp++;
            }
            playingStationIndex = selectedIndex = temp;
            stationTitle.setText(localStationList.get(temp).getName());
            if(localStationList.get(temp).containsOption("subheader")) {
                stationDescription.setText(localStationList.get(temp).getOption("subheader").toString());
            }

            playButton.setVisibility(View.INVISIBLE);
            gradientView.setVisibility(View.INVISIBLE);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(temp);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    selectedIndex = position;
                    if(localStationList.get(position).containsOption("subheader")) {
                        stationDescription.setVisibility(View.VISIBLE);
                        stationDescription.setText(localStationList.get(position).getOption("subheader").toString());
                    }
                    else {
                        stationDescription.setVisibility(View.GONE);
                        stationDescription.setText("");
                    }
                    stationTitle.setText(localStationList.get(position).getName());
                    if(position == playingStationIndex)
                    {
                        playButton.setVisibility(View.INVISIBLE);
                        gradientView.setVisibility(View.INVISIBLE);

                    }
                    else {

                        playButton.setVisibility(View.VISIBLE);
                        gradientView.setVisibility(View.VISIBLE);
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

        }
    }


    public class MyPagerAdapter extends PagerAdapter  {

        @Override
        public int getCount() {
            return localStationList.size();
        }


        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return object==view;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull final ViewGroup container, int position) {

            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            assignBackground(imageView, localStationList.get(position));
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((ImageView)object);
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private void assignBackground(final ImageView imageView, Station station) {
        // find a bitmap and assign it to 'bm'
        String bgUrl;

        try {
            bgUrl = (String) station.getOption("background_image_url");
        } catch (ClassCastException e) {
            bgUrl = null;
        }
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Drawable d = new BitmapDrawable(getResources(), bitmap);
                imageView.setBackground(d);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.gradient));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        if (bgUrl != null && imageView != null && !bgUrl.isEmpty()) {

            Picasso.with(getContext()).load(bgUrl).resize(400,400).centerCrop().into(target);

        } else if(imageView != null)
        {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_station_background);
            // update background image
            imageView.setImageBitmap(bm);
        }
    }

}
