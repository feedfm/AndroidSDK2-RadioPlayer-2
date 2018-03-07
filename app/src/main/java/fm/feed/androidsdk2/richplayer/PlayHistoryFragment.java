package fm.feed.androidsdk2.richplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedPlayerService;
import fm.feed.android.playersdk.models.AudioFile;
import fm.feed.android.playersdk.models.Play;
import fm.feed.android.playersdk.models.Station;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class PlayHistoryFragment extends Fragment {

    List<Station> stationList;
    @BindView(R.id.historyList)
    ExpandableListView listView;

    FeedAudioPlayer feedAudioPlayer;
    PlayHistoryAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayHistoryFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.play_history, container, false);
        ButterKnife.bind(this, view);
        if(((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("History");
        }
        FeedPlayerService.getInstance(new FeedAudioPlayer.AvailabilityListener() {

            @Override
            public void onPlayerAvailable(FeedAudioPlayer aFeedAudioPlayer) {
                feedAudioPlayer = aFeedAudioPlayer;
                stationList =  feedAudioPlayer.getStationList();
                feedAudioPlayer.addPlayListener(playListener);
                feedAudioPlayer.addLikeStatusChangeListener(likeStatusChangeListener);
                adapter = new PlayHistoryAdapter(feedAudioPlayer.getPlayHistory(), inflater);
                listView.setAdapter(adapter);
                int count = adapter.getGroupCount();
                for (int position = 0; position < count; position++)
                {
                    listView.expandGroup(position);
                }
            }

            @Override
            public void onPlayerUnavailable(Exception e) {

            }
        });

        // Set the adapter
        return view;
    }

    FeedAudioPlayer.PlayListener playListener = new FeedAudioPlayer.PlayListener() {
        @Override
        public void onSkipStatusChanged(boolean b) {

        }

        @Override
        public void onProgressUpdate(Play play, float v, float v1) {

        }

        @Override
        public void onPlayStarted(Play play) {
            adapter.setNewData(feedAudioPlayer.getPlayHistory());
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    FeedAudioPlayer.LikeStatusChangeListener likeStatusChangeListener = new FeedAudioPlayer.LikeStatusChangeListener() {
        @Override
        public void onLikeStatusChanged(AudioFile audioFile) {
            adapter.setNewData(feedAudioPlayer.getPlayHistory());
        }
    };

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public List<PlayByStations> separatePlaysByStations(List<Play> plays){
        List<PlayByStations> list = new ArrayList<>();
        boolean isAdded;
        for (Play play: plays) {
            isAdded = false;
            Station station = play.getStation();
            for (PlayByStations ps: list) {
                if(ps.station.equals(station))
                {
                    ps.playForThisStation.add(play);
                    isAdded = true;
                }
            }
            if(!isAdded)
            {
               PlayByStations pbS =  new PlayByStations();
               pbS.station = MainActivity.getStationById( station.getId(), stationList);
               pbS.playForThisStation.add(play);
               list.add(pbS);
            }
        }
        return list;
    }

    public class PlayByStations{
        Station station;
        List<Play> playForThisStation = new ArrayList<>();
    }

    public class PlayHistoryAdapter extends BaseExpandableListAdapter {

        List<PlayByStations> playHistory;
        LayoutInflater inflater;

        PlayHistoryAdapter(List<Play> list, LayoutInflater inflater) {
            playHistory = separatePlaysByStations(list);
            this.inflater = inflater;
        }

        void setNewData(List<Play> list) {
            playHistory = separatePlaysByStations(list);
            this.notifyDataSetChanged();
        }

        @Override
        public int getGroupCount() {
            return playHistory.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return playHistory.get(i).playForThisStation.size();
        }

        @Override
        public Object getGroup(int i) {
            return playHistory.get(i);
        }

        @Override
        public Object getChild(int i, int i1) {
            return playHistory.get(i).playForThisStation.get(i1);
        }

        @Override
        public long getGroupId(int i) {
            return playHistory.get(i).station.getId();
        }

        @Override
        public long getChildId(int i, int i1) {
            return playHistory.get(i).playForThisStation.get(i1).getAudioFile().getId().hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            GroupViewHolder holder;
            if (view == null) { // Check type
                holder = new GroupViewHolder();
                view = inflater.inflate(R.layout.history_group_item, viewGroup, false);
                ButterKnife.bind(holder, view);
                view.setTag(holder);
            }
            else {
                holder = (GroupViewHolder) view.getTag();
            }
            holder.textView.setText(playHistory.get(i).station.getName());
            assignArtWork(playHistory.get(i).station, holder.imageView);
            return view;
        }

        @Override
        public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
            ChildViewHolder holder;
            if (view == null) { // Check type
                holder = new ChildViewHolder();
                view = inflater.inflate(R.layout.history_child_item, viewGroup, false);
                ButterKnife.bind(holder, view);
                view.setTag(holder);
            }
            else {
                holder = (ChildViewHolder) view.getTag();
            }
            holder.songTv.setText(playHistory.get(i).playForThisStation.get(i1).getAudioFile().getTrack().getTitle());
            holder.artistTv.setText(playHistory.get(i).playForThisStation.get(i1).getAudioFile().getArtist().getName());
            holder.disLikeButton.setTag(playHistory.get(i).playForThisStation.get(i1).getAudioFile());
            holder.likeButton.setTag(playHistory.get(i).playForThisStation.get(i1).getAudioFile());

            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return false;
        }

        class GroupViewHolder{
            @BindView(R.id.play_history_station_name)
            TextView textView;
            @BindView(R.id.historyImageView)
            ImageView imageView;
        }

        class ChildViewHolder{
            @BindView((R.id.play_history_title_name))
            TextView songTv;
            @BindView(R.id.play_history_artist_name)
            TextView artistTv;
            @BindView(R.id.history_child_like)
            ImageButton likeButton;
            @BindView(R.id.history_child_dislike)
            ImageButton disLikeButton;

            @OnClick(R.id.history_child_like)
            public void OnLike(ImageButton button)
            {
                feedAudioPlayer.like((AudioFile)button.getTag());
            }
            @OnClick(R.id.history_child_dislike)
            public void OnDisLike(ImageButton button)
            {
                feedAudioPlayer.dislike((AudioFile)button.getTag());
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

        if (bgUrl != null && !bgUrl.isEmpty()) {

            Picasso.with(getContext()).load(bgUrl).resize(400, 400).centerCrop().into(imageView);

        } else {

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_station_background);
            imageView.setImageBitmap(bm);
        }
    }

}
