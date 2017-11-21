package fm.feed.androidsdk2.richplayer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fm.feed.android.playersdk.models.Station;



public class StationsFragment extends Fragment {


    private StationSelectionListener mListener;

    public StationsFragment() {
    }

    @BindView(R.id.station_grid)
    GridView gridView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_station, container, false);
        ButterKnife.bind(this, view);
        toolbarTitle.setText("Stations");
        StationAdaptor stationAdaptor = new StationAdaptor(((MainActivity)getActivity()).feedAudioPlayer.getStationList(), inflater, gridView);
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
        ((MainActivity)getActivity()).feedAudioPlayer.getStationList();

        return view;
    }

    public class StationAdaptor extends BaseAdapter {

        List<Station> stationList;
        LayoutInflater layoutInflater;
        ViewGroup container;

        StationAdaptor(List<Station> list, LayoutInflater inflater, ViewGroup container){
            stationList = list;
            layoutInflater = inflater;
            this.container = container;
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
        public View getView(int i, View view, ViewGroup viewGroup) {

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
            assignArtWork(stationList.get(i), holder.stationImage);
            return view;
        }

        class ViewHolder {
            @BindView(R.id.station_image_view)
            ImageView stationImage;
            @BindView(R.id.station_name)
            TextView stationName;
            @BindView(R.id.station_type)
            TextView stationType;
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

        if (bgUrl != null) {

            Glide.with(this).load(bgUrl).centerCrop().into(imageView);

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
