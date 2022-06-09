package social.entourage.android.new_v8.groups.details.posts

import android.graphics.Bitmap
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
import social.entourage.android.tools.Utils
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.URI


class CreatePostActivity : AppCompatActivity() {

    lateinit var binding: NewActivityCreatePostBinding
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var groupId = Const.DEFAULT_VALUE
    var imageURI: String? = null
    private var photoFile: File? = null
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_create_post
        )
        groupId = intent.getIntExtra(Const.GROUP_ID, Const.DEFAULT_VALUE)
        groupPresenter.hasPost.observe(this, ::handlePost)
        setView()
        handleDeleteImageButton()
        handleAddPhotoButton()
        getResult()
        validatePost()
        handleBackButton()
        handleMessageChangedTextListener()
    }

    private fun handlePost(hasPost: Boolean) {
        if (hasPost) {
            finish()
        }
    }

    private fun handleAddPhotoButton() {
        binding.addPhotoLayout.setOnClickListener {
            choosePhoto()
        }
        binding.photoLayout.setOnClickListener {
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

    private fun handleDeleteImageButton() {
        binding.deleteImage.setOnClickListener {
            binding.addPhotoLayout.visibility = View.VISIBLE
            binding.photoLayout.visibility = View.GONE
            imageURI = null
        }
    }

    private fun getResult() {
        supportFragmentManager.setFragmentResultListener(
            Const.REQUEST_KEY_CHOOSE_PHOTO,
            this
        ) { _, bundle ->
            imageURI = bundle.getString(Const.CHOOSE_PHOTO)
            imageURI?.let {
                binding.addPhotoLayout.visibility = View.GONE
                binding.photoLayout.visibility = View.VISIBLE
                Glide.with(this)
                    .load(Uri.parse(it))
                    .transform(CenterCrop(), RoundedCorners(14.px))
                    .into(binding.addPhoto)
                handleSaveButtonState(true)
                try {
                    contentResolver?.let { contentResolver ->
                        bitmap =
                            Utils.getBitmapFromUri(Uri.parse(it), contentResolver)
                    }
                } catch (e: IOException) {
                    Timber.e(e)
                }
            }
        }
    }

    private fun validatePost() {
        binding.validate.button.setOnClickListener {
            if (imageURI.isNullOrEmpty() && isMessageValid()) {
                val messageChat = ArrayMap<String, Any>()
                messageChat["content"] = binding.message.text.toString()
                val request = ArrayMap<String, Any>()
                request["chat_message"] = messageChat
                Timber.e(messageChat.toString())
                groupPresenter.addPost(groupId, request)
            }
            if (!imageURI.isNullOrEmpty()) {
                photoFile = bitmap?.let { it1 ->
                    Utils.saveBitmapToFile(it1, null)
                }
                photoFile?.let { photoFile ->
                    groupPresenter.addPost(
                        binding.message.text.toString(),
                        photoFile,
                        groupId
                    )
                }
            }
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
                handleSaveButtonState(isMessageValid() || !imageURI.isNullOrEmpty())
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