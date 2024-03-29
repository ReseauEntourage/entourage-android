package social.entourage.android.involvement

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.tools.log.AnalyticsEvents

class RateActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SHORTCUT_RATEAPP)
        val uri = Uri.parse(getString(R.string.market_url, packageName))
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.playstore_url, packageName))))
        }
        finish()
    }
}