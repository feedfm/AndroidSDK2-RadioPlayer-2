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
        String token = settings.getString("token", "offline");
        String secret = settings.getString("secret", "offline");

        FeedAudioPlayer.Builder builder = new FeedAudioPlayer.Builder().setToken(token)
                .setSecret(secret)
                .setContext(getApplicationContext());
        // Do not build, send builder to FeedPlayerService
        FeedPlayerService.initialize(builder);
    }

}
