package fm.feed.androidsdk2.richplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedPlayerService;
import fm.feed.android.playersdk.models.Station;

public class RemoteList extends AppCompatActivity {

    @BindView(R.id.remoteList)
    ListView listView;
    RemoteAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_list);
        ButterKnife.bind(this);
        FeedPlayerService.getInstance(new FeedAudioPlayer.AvailabilityListener() {

            @Override
            public void onPlayerAvailable(FeedAudioPlayer feedAudioPlayer) {
                if(feedAudioPlayer.getRemoteOfflineStationList().size() > 0)
                {
                    adapter = new RemoteAdapter(feedAudioPlayer.getRemoteOfflineStationList(), getLayoutInflater());
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            feedAudioPlayer.downloadAndSync((Station) adapter.getItem(position), stationDownloadListener);
                        }
                    });

                }
            }

            @Override
            public void onPlayerUnavailable(Exception e) {

            }
        });

    }


    FeedAudioPlayer.StationDownloadListener stationDownloadListener = new FeedAudioPlayer.StationDownloadListener() {

        /**
         *  totalDownloads
         *  pendingDownloads
         *  interruptedDownloads
         */

        @Override
        public void onDownloadProgress(Station station, int totalDownloads, int pendingDownloads, int interruptedDownloads) {

            float percent = ((totalDownloads - pendingDownloads - interruptedDownloads) *100) / totalDownloads;
            Progress progress = new Progress(station.getId(), percent);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    adapter.setProgress(progress);
                }
            });
        }
    };

    public class Progress {
        Progress(Integer sId, float progress) {
            id = sId;
            this.progress = progress;
        }
        private Integer id;
        private float progress;

        public Integer getId() {
            return id;
        }
        public float getProgress() {
            return progress;
        }
    }

    public class RemoteAdapter extends BaseAdapter {

        List<Station> stationArrayList;
        List<Progress> progresses = new ArrayList<>();
        LayoutInflater inflater;

        RemoteAdapter(List<Station> list, LayoutInflater inflater) {
            stationArrayList = list;
            this.inflater = inflater;
        }

        public void setProgress(Progress progress) {

            Iterator<Progress> itr  = progresses.iterator();
            while(itr.hasNext())
            {
                Progress pr = itr.next();
                if (pr.getId().equals(progress.getId())){

                    itr.remove();
                }
            }
            progresses.add(progress);
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return stationArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return stationArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return stationArrayList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new RemoteList.RemoteAdapter.ViewHolder();
                convertView = inflater.inflate(R.layout.remote_list_item, parent, false);
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            }
            else {
                holder = (RemoteList.RemoteAdapter.ViewHolder) convertView.getTag();
            }
            for (Progress pr:progresses) {
                if(pr.getId().equals(stationArrayList.get(position).getId())) {
                    holder.progressBar.setProgress((int) pr.getProgress());
                }
            }
            holder.textView.setText(stationArrayList.get(position).getName());
            return convertView;
        }

        class ViewHolder {
            @BindView(R.id.remote_station_name)
            TextView textView;
            @BindView(R.id.progressBar3)
            ProgressBar progressBar;
        }
    }
}
