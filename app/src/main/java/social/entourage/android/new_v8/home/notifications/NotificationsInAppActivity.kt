package social.entourage.android.new_v8.home.notifications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.add
import androidx.fragment.app.commit
import social.entourage.android.R
import social.entourage.android.new_v8.utils.Const

class NotificationsInAppActivity : AppCompatActivity(R.layout.activity_notifications_in_app) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getIntExtra(Const.NOTIF_COUNT,0)
        val bundle = Bundle().apply {
            putInt(Const.NOTIF_COUNT, id)
        }
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<NotifsInAppListFragment>(R.id.fragment_container_view,null,bundle)
            }
        }
    }
}