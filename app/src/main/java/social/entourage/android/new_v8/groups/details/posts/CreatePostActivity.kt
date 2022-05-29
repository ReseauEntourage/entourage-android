package social.entourage.android.new_v8.groups.details.posts

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityCreatePostBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.user.ReportUserModalFragment
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.px
import timber.log.Timber
import java.io.File


class CreatePostActivity : AppCompatActivity() {

    lateinit var binding: NewActivityCreatePostBinding
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var groupId = Const.DEFAULT_VALUE
    var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_create_post
        )
        groupId = intent.getIntExtra(Const.GROUP_ID, Const.DEFAULT_VALUE)
        setView()
        handleAddPhotoButton()
        getResult()
        validatePost()
        handleBackButton()
        handleMessageChangedTextListener()
    }

    private fun handleAddPhotoButton() {
        binding.addPhotoLayout.setOnClickListener {
            choosePhoto()
        }
        binding.addPhoto.setOnClickListener {
            choosePhoto()
        }
    }

    private fun choosePhoto() {
        val choosePhotoModalFragment = ChoosePhotoModalFragment.newInstance()
        choosePhotoModalFragment.show(supportFragmentManager, ReportUserModalFragment.TAG)
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

    private fun getResult() {
        supportFragmentManager.setFragmentResultListener(
            Const.REQUEST_KEY_CHOOSE_PHOTO,
            this
        ) { _, bundle ->
            Timber.e("dans get result")
            imagePath = bundle.getString(Const.CHOOSE_PHOTO)
            imagePath?.let {
                binding.addPhotoLayout.visibility = View.GONE
                binding.addPhoto.visibility = View.VISIBLE
                Glide.with(this)
                    .load(Uri.parse(it))
                    .transform(CenterCrop(), RoundedCorners(14.px))
                    .into(binding.addPhoto)
                handleSaveButtonState(true)
            }
        }
    }

    private fun validatePost() {
        binding.validate.button.setOnClickListener {
            val messageChat = ArrayMap<String, Any>()
            if (isMessageValid()) messageChat["content"] = binding.message.text
            val request = ArrayMap<String, Any>()
            request["chat_message"] = messageChat
            Timber.e(messageChat.toString())
            groupPresenter.addPost(groupId, request)
/*
            imagePath?.let {
                val file = File(it)
                groupPresenter.addPost(file, groupId)
            }
 */
        }
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            finish()
        }
    }

    private fun handleMessageChangedTextListener() {
        binding.message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                handleSaveButtonState(isMessageValid() || !imagePath.isNullOrBlank())
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun handleSaveButtonState(isActive: Boolean) {
        val background = ContextCompat.getDrawable(
            this,
            if (isActive) R.drawable.new_rounded_button_orange else R.drawable.new_bg_rounded_inactive_button_light_orange
        )
        binding.validate.button.background = background
    }

    private fun isMessageValid(): Boolean {
        return binding.message.text.isNotEmpty() && binding.message.text.isNotBlank()
    }

}