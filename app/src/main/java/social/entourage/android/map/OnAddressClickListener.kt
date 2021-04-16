package social.entourage.android.map

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import timber.log.Timber

import java.util.Locale

class OnAddressClickListener(private val context: Context, private val address: String, val isFromDetail:Boolean = false) : View.OnClickListener {

    override fun onClick(v: View) {
        Uri.parse(String.format(Locale.FRENCH, "geo:0,0?q=%s", address))?.let {uri ->
            openExternalMap(uri)
        }
    }

    private fun openExternalMap(geoLocation: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = geoLocation
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
        }

    }
}