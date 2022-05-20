package social.entourage.android.new_v8.groups.details.posts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityCreatePostBinding


class CreatePostActivity : AppCompatActivity() {

    lateinit var binding: NewActivityCreatePostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_create_post
        )
    }
}