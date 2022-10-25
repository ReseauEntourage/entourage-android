package social.entourage.android.new_v8.actions.create

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import social.entourage.android.R
import social.entourage.android.new_v8.utils.Const

class CreateActionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_create_action)

        val isDemand = intent.getBooleanExtra(Const.IS_ACTION_DEMAND,false)
    }
}