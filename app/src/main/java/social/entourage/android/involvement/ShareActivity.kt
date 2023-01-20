package social.entourage.android.involvement

import android.content.Intent
import android.os.Bundle
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.tools.log.AnalyticsEvents

class ShareActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SHORTCUT_SHAREAPP)
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