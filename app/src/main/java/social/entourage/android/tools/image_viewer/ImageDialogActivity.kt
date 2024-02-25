package social.entourage.android.tools.image_viewer

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Post
import social.entourage.android.base.BaseActivity
import social.entourage.android.comment.CommentsListAdapter
import social.entourage.android.databinding.ImageDialogFragmentBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.groups.GroupPresenter
import timber.log.Timber

class ImageDialogActivity:BaseActivity() {

    private lateinit var binding:ImageDialogFragmentBinding
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImageDialogFragmentBinding.inflate(layoutInflater)
        val postId = intent.getIntExtra("postId", 0)
        val eventId = intent.getIntExtra("eventId", 0)
        val groupId = intent.getIntExtra("groupId", 0)
        eventPresenter.getCurrentParentPost.observe(this, ::handleParentPost)
        groupPresenter.getCurrentParentPost.observe(this, ::handleParentPost)

        if(eventId != 0){
            eventPresenter.getCurrentParentPost(eventId,postId)
        }else if(groupId != 0){
            groupPresenter.getCurrentParentPost(groupId,postId)
        }

        setCloseButton()
        setContentView(binding.root)
    }

    fun setCloseButton(){
        binding.btnClose.setOnClickListener {
            finish()
        }
    }


    fun setView(imageUrl:String){
        val displayMetrics = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val params = binding.photoView.layoutParams
        params.width = screenWidth
        params.height = screenHeight
        binding.photoView.layoutParams = params
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = Glide.with(applicationContext)
                    .asBitmap()
                    .load(imageUrl)
                    .submit(screenWidth,screenHeight)
                    .get()
                withContext(Dispatchers.Main) {
                    binding.photoView.setImageBitmap(bitmap)
                }
            } catch(e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun handleParentPost(currentPost: Post?) {
        currentPost?.imageUrl?.let {
            CoroutineScope(Dispatchers.Main).launch {
                setView(it)
            }
        }
    }
}