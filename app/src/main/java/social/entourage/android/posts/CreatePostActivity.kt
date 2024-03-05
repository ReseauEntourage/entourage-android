package social.entourage.android.posts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.base.ChoosePhotoModalFragment
import social.entourage.android.databinding.ActivityCreatePostBinding
import social.entourage.android.groups.details.feed.CreatePostGroupActivity
import social.entourage.android.groups.details.feed.FeedActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.px
import java.io.File

abstract class CreatePostActivity : AppCompatActivity() {

    lateinit var binding: ActivityCreatePostBinding
    protected var groupId = Const.DEFAULT_VALUE
    private var shouldClose = false
    var imageURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_FEED_NEW_POST_SCREEN
        )
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getIntExtra(Const.ID, Const.DEFAULT_VALUE)
        shouldClose = intent.getBooleanExtra(Const.FROM_CREATE_GROUP, false)
        setView()
        handleDeleteImageButton()
        handleAddPhotoButton()
        getResult()
        validatePost()
        handleBackButton()
        handleMessageChangedTextListener()
    }

    protected fun handlePost(hasPost: Boolean) {

        if (hasPost) {
            if (shouldClose) {
                var id = groupId
                if(id == -1 && CreatePostGroupActivity.idGroupForPost != null){
                    id = CreatePostGroupActivity.idGroupForPost!!
                }
                // Créer un Handler et l'utiliser pour retarder l'intention
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(
                        Intent(this, FeedActivity::class.java).putExtra(
                            Const.GROUP_ID,
                            id
                        )
                    )
                    finish()
                }, 2000) // 2000 ms = 2 secondes
            }
            CreatePostGroupActivity.idGroupForPost = null
            finish()
        }
    }

    private fun handleAddPhotoButton() {
        binding.addPhotoLayout.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_FEED_NEW_POST_ADD_PIC
            )
            choosePhoto()
        }
        binding.photoLayout.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_FEED_NEW_POST_ADD_PIC
            )
            choosePhoto()
        }
    }

    private fun choosePhoto() {
        val choosePhotoModalFragment = ChoosePhotoModalFragment.newInstance()
        choosePhotoModalFragment.show(supportFragmentManager, ChoosePhotoModalFragment.TAG)
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
            imageURI = bundle.getParcelable(Const.CHOOSE_PHOTO)
            imageURI?.let {
                binding.addPhotoLayout.visibility = View.GONE
                binding.photoLayout.visibility = View.VISIBLE
                Glide.with(this)
                    .load(it)
                    .transform(CenterCrop(), RoundedCorners(14.px))
                    .into(binding.addPhoto)
                handleSaveButtonState(true)
            }
        }
    }

    private fun validatePost() {
        binding.validate.button.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_FEED_NEW_POST_VALIDATE
            )
            if (imageURI == null && isMessageValid()) {
                val messageChat = ArrayMap<String, Any>()
                messageChat["content"] = binding.message.text.toString()
                val request = ArrayMap<String, Any>()
                request["chat_message"] = messageChat
                addPostWithoutImage(request)

            }
            if (imageURI != null) {
                imageURI?.let { it1 ->
                    val file = Utils.getFile(this, it1)
                    addPostWithImage(file)

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
                handleSaveButtonState(isMessageValid() || imageURI != null)
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

    abstract fun addPostWithImage(file: File)
    abstract fun addPostWithoutImage(request: ArrayMap<String, Any>)
}