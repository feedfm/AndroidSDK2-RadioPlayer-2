package fm.feed.androidsdk2.richplayer;

import android.app.Application;
import android.content.SharedPreferences;

import fm.feed.android.playersdk.FeedAudioPlayer;
import fm.feed.android.playersdk.FeedPlayerService;

/**
 * Created by arv on 11/3/17.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences settings;
        settings = getSharedPreferences("FEEDCREDS", MODE_PRIVATE);
        String token = settings.getString("token", "demo");
        String secret = settings.getString("secret", "demo");

        FeedAudioPlayer.Builder builder = new FeedAudioPlayer.Builder(getApplicationContext(), token, secret);
        // Do not build, send builder to FeedPlayerService
        FeedPlayerService.initialize(builder);
    }

}
