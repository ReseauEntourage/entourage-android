package social.entourage.android.involvement

import android.content.Intent
import android.os.Bundle
import social.entourage.android.base.EntourageActivity
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R

class ShareActivity : EntourageActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EntourageEvents.logEvent(EntourageEvents.EVENT_SHORTCUT_SHAREAPP)
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.playstore_url, packageName))
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.entourage_share_intent_title)))
        finish()
    }
}