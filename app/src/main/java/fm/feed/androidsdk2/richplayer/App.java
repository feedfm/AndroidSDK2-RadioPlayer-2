package fm.feed.androidsdk2.richplayer;

import android.app.Application;

import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedPlayerService;

/**
 * Created by arv on 11/3/17.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FeedPlayerService.initialize(getApplicationContext(),"offline","offline");
    }

}
