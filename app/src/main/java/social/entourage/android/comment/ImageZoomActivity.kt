package social.entourage.android.comment

import android.os.Bundle
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityImageZoomBinding

class ImageZoomActivity : BaseActivity() {

    private lateinit var binding: ActivityImageZoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageZoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra("image_url")

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.place_holder_large)
            .error(R.drawable.place_holder_large)
            .into(binding.fullscreenImage)

        binding.fullscreenImage.setOnClickListener {
            finish()
        }
    }
}
