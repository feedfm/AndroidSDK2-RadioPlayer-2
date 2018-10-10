package fm.feed.androidsdk2.richplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import fm.feed.android.playersdk.FeedPlayerService;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrreaderActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {


    private static final String TAG = "QRREADER";
    private ZXingScannerView mScannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
        mScannerView.setAutoFocus(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }
        @Override
    public void handleResult(Result result) {


        mScannerView.stopCamera();
        Log.v(TAG, result.getText()); // Prints scan results

        String str = result.getText();
        String[] stra = str.split(",");
        if(stra.length == 2)
        {
            String token = stra[0];
            String secret = stra[1];
            Toast.makeText(QrreaderActivity.this, token +" , "+secret, Toast.LENGTH_SHORT).show();
            SharedPreferences settings;
            settings = getSharedPreferences("FEEDCREDS", MODE_PRIVATE);
            settings.edit().putString("token", token).apply();
            settings.edit().putString("secret", secret).apply();
            FeedPlayerService.initialize(getApplicationContext(),token,secret);
        }
        finish();

    }
}
