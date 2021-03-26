package fm.feed.androidsdk2.richplayer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import fm.feed.android.playersdk.AvailabilityListener;
import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedPlayerService;
import fm.feed.android.playersdk.LikeStatusChangeListener;
import fm.feed.android.playersdk.PlayListener;
import fm.feed.android.playersdk.State;
import fm.feed.android.playersdk.StateListener;
import fm.feed.android.playersdk.models.AudioFile;
import fm.feed.android.playersdk.models.Play;
import fm.feed.android.playersdk.models.Station;


/**
 * A simple {@link Fragment} subclass.
 */
public class OnDemandFragment extends Fragment {

    RecyclerAdapter mAdapter;

    @BindView(R.id.demand_list)
    RecyclerView recyclerListView;

    @BindView((R.id.station_art_demand))
    ImageView stationArt;

    @BindView(R.id.station_demand_name)
    TextView stationName;

    FeedAudioPlayer mFeedAudioPlayer;

    public OnDemandFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_on_demand, container, false);
        ButterKnife.bind(this, view);
        if((getActivity() != null) && ((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Playlist");
        }

        FeedPlayerService.getInstance(new AvailabilityListener() {
            @Override
            public void onPlayerAvailable(final FeedAudioPlayer feedAudioPlayer) {
                mFeedAudioPlayer = feedAudioPlayer;
                assignStationBG(feedAudioPlayer.getActiveStation());
                stationName.setText(feedAudioPlayer.getActiveStation().getName());
                if(feedAudioPlayer.getActiveStation().getAudioFiles() != null) {
                    feedAudioPlayer.addPlayListener(playListener);
                    feedAudioPlayer.addLikeStatusChangeListener(likeStatusChangeListener);
                    feedAudioPlayer.addStateListener(stateListener);

                    mAdapter = new RecyclerAdapter(feedAudioPlayer.getActiveStation().getAudioFiles());
                    recyclerListView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerListView.setAdapter(mAdapter);
                    if(feedAudioPlayer.getCurrentPlay() != null) {
                        mAdapter.setActivePlayingID(feedAudioPlayer.getCurrentPlay().getAudioFile().getId());
                    }
                    mAdapter.SetOnItemClickListener(new OnItemViewClickListener() {
                        @Override
                        public void onItemClick(View view, int position, AudioFile file) {
                            Log.d("DEMAND", " "+file.getId() +" P= "+ position);
                            switch (view.getId())
                            {
                                case R.id.playSongOnDemand:
                                    feedAudioPlayer.play( file);
                                    break;
                                case R.id.ondemand_dislike:
                                    feedAudioPlayer.dislike(file);
                                    break;
                                case R.id.ondemand_like:
                                    feedAudioPlayer.like(file);
                            }
                        }
                    });

                    stateListener.onStateChanged(feedAudioPlayer.getState());
                }
                else {
                    Toast.makeText(getContext(), "This is not an on demand station", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPlayerUnavailable(Exception e) {

            }
        });

        return view;
    }

    StateListener stateListener = new StateListener() {
        @Override
        public void onStateChanged(State state) {
            if(state == State.PLAYING)
            {
                mAdapter.setPlaying(true);
            }
            else if(state == State.PAUSED)
            {
                mAdapter.setPlaying(false);
            }
        }
    };

    PlayListener playListener = new PlayListener() {

        @Override
        public void onSkipStatusChanged(boolean b) {
        }

        @Override
        public void onProgressUpdate(Play play, float elapsedTime, float duration) {

        }

        @Override
        public void onPlayStarted(Play play) {
            mAdapter.setActivePlayingID(play.getAudioFile().getId());
        }
    };

    LikeStatusChangeListener likeStatusChangeListener = new LikeStatusChangeListener() {
        @Override
        public void onLikeStatusChanged(AudioFile audioFile) {
            mAdapter.setUpdatedAudioFile(audioFile);
        }
    };


    public interface OnItemViewClickListener {
        void onItemClick(View view, int position, AudioFile file);
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        private List<AudioFile> list;
        private  OnItemViewClickListener mItemClickListener;
        private String activePlayingID;
        boolean isPlaying = true;


        private void setPlaying(boolean playing) {
            if(isPlaying != playing) {
                isPlaying = playing;
                notifyDataSetChanged();
            }
        }

        private void setActivePlayingID(String playingID) {

            int i = 0, oldPos = 0, newPos = 0;
            for(AudioFile file:list)
            {
                if(file.getId().equals(playingID))
                {
                    newPos = i;
                }
                if(file.getId().equals(activePlayingID))
                {
                    oldPos = i;
                }
                i++;
            }
            this.activePlayingID = playingID;
            mAdapter.notifyItemChanged(newPos);
            mAdapter.notifyItemChanged(oldPos);
        }

        private void setUpdatedAudioFile(AudioFile file) {
            int i = 0;
            for (AudioFile audioFile:list) {
                if(audioFile.equals(file))
                {
                    list.remove(i);
                    list.add(i,file);
                    notifyItemChanged(i);
                    break;
                }
                i++;
            }
        }

        private RecyclerAdapter(List<AudioFile> itemsData) {
            this.list = itemsData;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            // create a new views
            View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.demand_list_item, parent, false);

            // create ViewHolder
            return new ViewHolder(itemLayoutView);

        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {

            viewHolder.songTv.setText(list.get(position).getTrack().getTitle());
            viewHolder.artistTv.setText(list.get(position).getArtist().getName());
            if (list.get(position).isDisliked()) {
                viewHolder.disLikeButton.setImageResource(R.drawable.dislike_filled_black);
            } else if (list.get(position).isLiked()) {
                viewHolder.likeButton.setImageResource(R.drawable.like_filled_black);
            }else {
                viewHolder.likeButton.setImageResource(R.drawable.like_unfilled_black);
                viewHolder.disLikeButton.setImageResource(R.drawable.dislike_unfilled_black);
            }
            if(activePlayingID != null && isPlaying && activePlayingID.equals(list.get(position).getId())) {
                viewHolder.playOnDemand.setVisibility(View.GONE);
                viewHolder.play_progress.setVisibility(View.VISIBLE);
                viewHolder.play_progress.isPlaying(true);
            }
            else {
                viewHolder.playOnDemand.setVisibility(View.VISIBLE);
                viewHolder.play_progress.setVisibility(View.GONE);
            }
            assignArtWork(list.get(position) , viewHolder.imageView);

        }

        // inner class to hold a reference to each item of RecyclerView
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            @BindView(R.id.play_progress)
            CircularProgressView play_progress;
            @BindView(R.id.ondemand_image)
            ImageView imageView;
            @BindView(R.id.ondemand_title)
            TextView songTv;
            @BindView(R.id.ondemand_artist)
            TextView artistTv;
            @BindView(R.id.ondemand_like)
            ImageButton likeButton;
            @BindView(R.id.ondemand_dislike)
            ImageButton disLikeButton;
            @BindView(R.id.playSongOnDemand)
            RelativeLayout playOnDemand;

            private ViewHolder(View view){
                super(view);
                ButterKnife.bind(this,view);
                likeButton.setOnClickListener(this);
                playOnDemand.setOnClickListener(this);
                disLikeButton.setOnClickListener(this);
                play_progress.setInDeterminateAnim(false);
            }


            @Override
            public void onClick(View v) {
                this.getAdapterPosition();
                mItemClickListener.onItemClick(v, getAdapterPosition(), list.get(getAdapterPosition())); //OnItemViewClickListener mItemClickListener;
            }


        }

        private void SetOnItemClickListener(final OnItemViewClickListener mItemClickListener) {
            this.mItemClickListener = mItemClickListener;
        }

        // Return the size of your itemsData (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return list.size();
        }
    }


    private void assignArtWork(AudioFile audioFile, ImageView imageView) {
        // find a bitmap and assign it to 'bm'
        String bgUrl = null;
        try {
            Map<String, Object> map = audioFile.getMetadata();
            if(map.containsKey("background_image_url"))
            {
                bgUrl = (String) map.get("background_image_url");
            }

        } catch (ClassCastException e) {
            bgUrl = null;
        }

        loadImage(bgUrl, imageView);

    }

    private void assignStationBG(Station station){
        String bgUrl;

        try {
            bgUrl = (String) station.getOption("background_image_url");
        } catch (ClassCastException e) {
            bgUrl = null;
        }
        loadImage(bgUrl, stationArt);
    }

    private void loadImage(String url, ImageView imageView){

        if (url != null && !url.isEmpty()) {
            Picasso.get().load(url).resize(300,250).centerCrop().into(imageView);

        } else {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_station_background);
            imageView.setImageBitmap(bm);
        }
    }
}
