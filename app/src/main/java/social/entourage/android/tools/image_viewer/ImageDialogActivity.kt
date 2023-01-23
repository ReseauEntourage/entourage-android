package social.entourage.android.tools.image_viewer

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ImageDialogFragmentBinding

class ImageDialogActivity:BaseActivity() {

    private lateinit var binding:ImageDialogFragmentBinding
    private var imageUrl:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImageDialogFragmentBinding.inflate(layoutInflater)
        val imageUrl = intent.getStringExtra("image_url")
        if (imageUrl != null) {
            setImageUrl(imageUrl)
            setCloseButton()
            setView()
        }else{
            finish()
        }
        setContentView(binding.root)
    }

    fun setCloseButton(){
        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    fun setImageUrl(url:String){
        this.imageUrl = url
    }

    fun setView(){
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
            val bitmap = Glide.with(applicationContext)
                .asBitmap()
                .load(imageUrl)
                .submit(screenWidth,screenHeight)
                .get()
            withContext(Dispatchers.Main) {
                binding.photoView.setImageBitmap(bitmap)
            }
        }
    }

}