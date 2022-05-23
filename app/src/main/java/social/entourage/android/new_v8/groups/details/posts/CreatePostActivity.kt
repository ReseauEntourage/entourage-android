package social.entourage.android.new_v8.groups.details.posts

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.rounded_button_icon.view.*
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityCreatePostBinding
import social.entourage.android.new_v8.user.ReportUserModalFragment
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.px


class CreatePostActivity : AppCompatActivity() {

    lateinit var binding: NewActivityCreatePostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_create_post
        )
        setView()
        handleAddPhotoButton()
    }

    private fun handleAddPhotoButton() {
        binding.addPhotoLayout.setOnClickListener {
            val choosePhotoModalFragment = ChoosePhotoModalFragment.newInstance()
            choosePhotoModalFragment.show(supportFragmentManager, ReportUserModalFragment.TAG)
        }
    }

    private fun setView() {
        binding.validate.button.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ResourcesCompat.getDrawable(
                resources, R.drawable.new_post, null
            ),
            null
        )
    }
}