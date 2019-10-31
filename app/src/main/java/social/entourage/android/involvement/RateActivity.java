package social.entourage.android.involvement;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;

public class RateActivity extends EntourageActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EntourageEvents.logEvent(EntourageEvents.EVENT_SHORTCUT_RATEAPP);

        Uri uri = Uri.parse(getString(R.string.market_url, getPackageName()));
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.playstore_url,getPackageName()))));
        }
        finish();
    }
}
