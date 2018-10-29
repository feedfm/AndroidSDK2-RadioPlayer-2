package fm.feed.androidsdk2.richplayer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedPlayerService;
import fm.feed.android.playersdk.models.Play;
import fm.feed.android.playersdk.models.Station;



public class StationsFragment extends Fragment {

      private StationSelectionListener mListener;


    public StationsFragment() {

    }

    private static final String ISMODEOFFLINE = "isModeOffline";

    public static StationsFragment newInstance(Boolean isOfflineMode) {
        StationsFragment fragment = new StationsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ISMODEOFFLINE, isOfflineMode);
        fragment.setArguments(args);
        return fragment;
    }


    @BindView(R.id.station_grid)
    GridView gridView;

    @BindView(R.id.powered_by)
    TextView powered_by;
    StationAdaptor stationAdaptor;
    FeedAudioPlayer feedAudioPlayer;
    List<Station> localStationList;
    Boolean isOfflineMode;

    @OnClick(R.id.powered_by)
    public void onPoweredBy(){
        Intent ai = new Intent(getContext(), PoweredByFeedActivity.class);
        startActivity(ai);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isOfflineMode = getArguments().getBoolean(ISMODEOFFLINE);
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_station, container, false);
        ButterKnife.bind(this, view);
        if(getActivity() !=null && ((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Stations");
        }
        feedAudioPlayer = FeedPlayerService.getInstance();
        if(!isOfflineMode) {
            feedAudioPlayer.addAvailabilityListener(new FeedAudioPlayer.AvailabilityListener() {
                @Override
                public void onPlayerAvailable(FeedAudioPlayer feedAudioPlayer) {
                    localStationList = feedAudioPlayer.getStationList();
                    setup(inflater);
                }

                @Override
                public void onPlayerUnavailable(Exception e) {

                }
            });
        }
        else{
            if (feedAudioPlayer.getLocalOfflineStationList().size() > 0)
            {
                localStationList = feedAudioPlayer.getLocalOfflineStationList();
                setup(inflater);
            }
        }

        return view;
    }

    private void setup(LayoutInflater inflater) {

        stationAdaptor = new StationAdaptor(localStationList, inflater, gridView);
        Station station = feedAudioPlayer.getActiveStation();
        stationChangedListener.onStationChanged(station);
        feedAudioPlayer.addStationChangedListener(stationChangedListener);
        feedAudioPlayer.addStateListener(stateListener);
        feedAudioPlayer.addPlayListener(playListener);

        gridView.setAdapter(stationAdaptor);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
        gridView.setHorizontalSpacing((int)px);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                onStationSelected(l);
            }
        });
    }

    FeedAudioPlayer.StationChangedListener stationChangedListener = new FeedAudioPlayer.StationChangedListener() {
        @Override
        public void onStationChanged(Station station) {
            stationAdaptor.setActiveStation();
        }
    };

    FeedAudioPlayer.StateListener stateListener = new FeedAudioPlayer.StateListener() {
        @Override
        public void onStateChanged(FeedAudioPlayer.State state) {
            if(state == FeedAudioPlayer.State.PLAYING){
                stationAdaptor.setIsPlaying(true);
            }
            else
            {
                stationAdaptor.setIsPlaying(false);
            }
        }
    };

    FeedAudioPlayer.PlayListener playListener = new FeedAudioPlayer.PlayListener() {
        @Override
        public void onSkipStatusChanged(boolean b) {

        }

        @Override
        public void onProgressUpdate(Play play, float v, float v1) {

                stationAdaptor.setProgress(v, v1);
        }

        @Override
        public void onPlayStarted(Play play) {

        }
    };

    public class StationAdaptor extends BaseAdapter {

        List<Station> stationList;
        LayoutInflater layoutInflater;
        ViewGroup container;

        // Not sure if this is a good idea but better then redrawing the whole grid every time progress changes
        CircularProgressView circularProgressView;

        StationAdaptor(List<Station> list, LayoutInflater inflater, ViewGroup container){
            stationList = list;
            layoutInflater = inflater;
            this.container = container;
        }

        public void setActiveStation() {
            this.notifyDataSetInvalidated();
        }

        public void setIsPlaying(boolean playing) {
            if(circularProgressView != null) {
                circularProgressView.isPlaying(playing);
                this.notifyDataSetInvalidated();
            }
        }

        public void setProgress(float progress1, float maxProgress)
        {
            //progress = progress1;
            if(circularProgressView != null && circularProgressView.getVisibility() == View.VISIBLE)
            {
                circularProgressView.setMaxProgress(maxProgress);
                circularProgressView.setProgress(progress1);
            }
        }

        @Override
        public int getCount() {
            return stationList.size();
        }

        @Override
        public Object getItem(int i) {
            return stationList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return stationList.get(i).getId();
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            ViewHolder holder;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                view = layoutInflater.inflate(R.layout.station_item, container, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            holder.stationName.setText( stationList.get(i).getName());
            if(stationList.get(i).containsOption("subheader")) {
                holder.stationType.setText(stationList.get(i).getOption("subheader").toString());
            }

            if((feedAudioPlayer.getState().equals(FeedAudioPlayer.State.PLAYING) || feedAudioPlayer.getState().equals(FeedAudioPlayer.State.PAUSED)) &&(stationList.get(i).getId().equals(feedAudioPlayer.getActiveStation().getId()))){
                if(feedAudioPlayer.getState().equals(FeedAudioPlayer.State.PLAYING)) {
                    holder.circularProgressView.isPlaying(true);
                }
                else if(feedAudioPlayer.getState().equals(FeedAudioPlayer.State.PAUSED))
                {
                    holder.circularProgressView.isPlaying(false);
                }
                holder.playWhite.setVisibility(View.GONE);
                holder.circularProgressView.setVisibility(View.VISIBLE);
                holder.circularProgressView.setMaxProgress(feedAudioPlayer.getCurrentPlayDuration());
                holder.circularProgressView.setProgress(feedAudioPlayer.getCurrentPlaybackTime());
                this.circularProgressView = holder.circularProgressView;
            }
            else {
                holder.playWhite.setVisibility(View.VISIBLE);
                holder.circularProgressView.setVisibility(View.INVISIBLE);
            }
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStationSelected(getItemId(i));
                }
            };
            holder.playWhite.setOnClickListener(listener);
            /*holder.circularProgressView.setOnClickListener(listener);
            holder.stationImage.setOnClickListener(listener);
            holder.play.setOnClickListener(listener);*/
            assignArtWork(stationList.get(i), holder.stationImage);
            return view;
        }

        class ViewHolder {
            @BindView(R.id.play_station_layout)
            RelativeLayout playWhite;
            @BindView(R.id.circular_progress)
            CircularProgressView circularProgressView;
            @BindView(R.id.station_image_view)
            ImageView stationImage;
            @BindView(R.id.station_name)
            TextView stationName;
            @BindView(R.id.station_type)
            TextView stationType;
            @BindView(R.id.play_station)
            ImageView play;
            ViewHolder(View view){
                ButterKnife.bind(this, view);
            }
        }
    }

    private void assignArtWork(Station station, ImageView imageView) {
        // find a bitmap and assign it to 'bm'
        String bgUrl = null;
        try {
            if(station.containsOption("background_image_url")) {
                bgUrl = (String) station.getOption("background_image_url");
            }

        } catch (ClassCastException e) {
            bgUrl = null;
        }

        if (bgUrl != null && !bgUrl.trim().equals("")) {

            Picasso.with(getContext()).load(bgUrl).resize(400, 400).centerCrop().into(imageView);

        } else {

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_station_background);
            imageView.setImageBitmap(bm);
        }
    }

    public void onStationSelected(long stationId) {
        if (mListener != null) {
            mListener.onStationSelected(stationId);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StationSelectionListener) {
            mListener = (StationSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement StationSelectionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface StationSelectionListener {
        void onStationSelected(long stationId);
    }
}
