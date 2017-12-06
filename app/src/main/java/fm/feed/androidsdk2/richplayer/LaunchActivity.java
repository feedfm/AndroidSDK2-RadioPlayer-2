package fm.feed.androidsdk2.richplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LaunchActivity extends AppCompatActivity {

    @BindView(R.id.open_onDemand)
    Button onDemandButton;
    @BindView(R.id.open_station)
    Button stationsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.open_onDemand)
    public void OpenOnDemandStation()
    {
        Intent ai = new Intent(this, MainActivity.class);
        ai.putExtra("Target","OnDemand");
        startActivity(ai);
    }

    @OnClick(R.id.open_station)
    public void OpenStationsView()
    {
        Intent ai = new Intent(this, MainActivity.class);
        startActivity(ai);
    }
}
