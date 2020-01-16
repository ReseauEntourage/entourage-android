package social.entourage.android.involvement;

import android.content.Intent;
import android.os.Bundle;

import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;

public class ShareActivity extends EntourageActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EntourageEvents.logEvent(EntourageEvents.EVENT_SHORTCUT_SHAREAPP);

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.playstore_url,getPackageName()));

        startActivity(Intent.createChooser(sharingIntent, getString(R.string.entourage_share_intent_title)));
        finish();
    }
}
