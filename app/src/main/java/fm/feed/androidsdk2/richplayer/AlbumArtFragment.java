package fm.feed.androidsdk2.richplayer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

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
    AlbumArtFragmentListener mListener;
    int selectedIndex;
    int playingStationIndex;
    List<Station> localStationList;


    @BindView(R.id.albumArtFlipper)     ViewPager viewPager;
    @BindView(R.id.progressBar)         ProgressBar mProgressBar;
    @BindView(R.id.station_name_player) TextView stationTitle;
    @BindView(R.id.nextStation)         ImageButton nextStation;
    @BindView(R.id.previousStation)     ImageButton previousStation;
    @BindView(R.id.shareButton)         ImageButton shareButton;
    @BindView(R.id.viewStations)        ImageButton viewStations;
    @BindView(R.id.playArtStation)      ImageButton playButton;

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
        mPlayer.setActiveStation(localStationList.get(selectedIndex));
        view.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.shareButton)
    public void shareButton()
    {

    }

    @OnClick(R.id.nextStation)
    public void nextStation()
    {
        viewPager.setCurrentItem(++selectedIndex);
    }

    @OnClick(R.id.previousStation)
    public void previousStation()
    {
        viewPager.setCurrentItem(--selectedIndex);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_album_art, container, false);
        ButterKnife.bind(this, view);


        FeedPlayerService.getInstance(new FeedAudioPlayer.AvailabilityListener() {
            @Override
            public void onPlayerAvailable(FeedAudioPlayer feedAudioPlayer) {
                mPlayer = feedAudioPlayer;
                mPlayer.addPlayListener(playListener);
                mPlayer.addStationChangedListener(stationChangedListener);
                localStationList = mPlayer.getStationList();
                setUI();
            }

            @Override
            public void onPlayerUnavailable(Exception e) {

            }
        });

        return view;
    }

    public void setUI() {
        if(mPlayer!= null && stationTitle !=null) {

            final MyPagerAdapter pagerAdapter = new MyPagerAdapter();
            int temp = 0;
            for (Station st: localStationList) {
                if(st.getId() == mStationID) {
                    break;
                }
                temp++;
            }
            playingStationIndex = selectedIndex = temp;
            stationTitle.setText(localStationList.get(temp).getName());
            playButton.setVisibility(View.INVISIBLE);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(temp);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                    stationTitle.setText(localStationList.get(position).getName());
                    if(position != playingStationIndex )
                    {
                        playButton.setVisibility(View.VISIBLE);

                        //setColorFilter(semiTransparentGrey, PorterDuff.Mode.SRC_ATOP);
                    }
                    else {

                        playButton.setVisibility(View.INVISIBLE);
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
        public boolean isViewFromObject(View view, Object object) {
            return object==view;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {

            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            assignBackground(imageView, localStationList.get(position));
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView)object);
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof AlbumArtFragmentListener) {
            mListener = (AlbumArtFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private void assignBackground(ImageView imageView, Station station) {
        // find a bitmap and assign it to 'bm'
        String bgUrl;

        try {
            bgUrl = (String) station.getOption("background_image_url");
        } catch (ClassCastException e) {
            bgUrl = null;
        }

        if (bgUrl != null && imageView != null) {

            Glide.with(this).load(bgUrl).centerCrop().into(imageView);

        } else if(imageView != null)
        {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_station_background);
            // update background image
            imageView.setImageBitmap(bm);
        }
    }

    public interface AlbumArtFragmentListener {
        void onStationChangeRequestedFragment(Station station);
    }
}
