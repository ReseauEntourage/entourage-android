package social.entourage.android.notifications

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import social.entourage.android.R
import social.entourage.android.tools.utils.Const

class InAppNotificationsActivity : AppCompatActivity(R.layout.activity_notifications_in_app) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getIntExtra(Const.NOTIF_COUNT,0)
        val bundle = Bundle().apply {
            putInt(Const.NOTIF_COUNT, id)
        }
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<InAppNotificationListFragment>(R.id.fragment_container_view,null,bundle)
            }
        }
    }
}