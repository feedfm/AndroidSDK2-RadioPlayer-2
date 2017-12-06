package fm.feed.androidsdk2.richplayer;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by eml.
 * Copyright 2017, Feed Media LLC
 */

public class PoweredByFeedActivity extends AppCompatActivity {

    private final static String TAG = PoweredByFeedActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_powered_by_feed);

        if (getSupportActionBar() != null) {
            Log.e(TAG, "Please assign a theme to this activity with no action bar.");
        }

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("");
        ab.setHomeAsUpIndicator(R.drawable.feedfm_ic_close_white_24dp);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return true;
    }

}
