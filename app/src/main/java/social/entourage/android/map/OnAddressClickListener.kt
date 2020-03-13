package social.entourage.android.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View

import java.util.Locale

class OnAddressClickListener(private val context: Context, private val address: String) : View.OnClickListener {

    override fun onClick(v: View) {
        val uri:Uri? = Uri.parse(String.format(Locale.FRENCH, "geo:0,0?q=%s", address))
        if (uri != null) {
            openExternalMap(uri)
        }
    }

    private fun openExternalMap(geoLocation: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = geoLocation
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}