package social.entourage.android.tools.image_viewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import social.entourage.android.databinding.ImageDialogFragmentBinding
import java.net.URL


class ImageDialogFragment(private var imageUrl:String) : DialogFragment(){

    private lateinit var binding : ImageDialogFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = ImageDialogFragmentBinding.inflate(layoutInflater)
        //calculateUrl(imageUrl)
        setView(imageUrl)
        return binding.root
    }


    fun calculateUrl(imageUrl:String){
        CoroutineScope(Dispatchers.IO).launch {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(URL(imageUrl).openConnection().getInputStream(), null, options)
            val imageWidth = options.outWidth
            val imageHeight = options.outHeight
            //setView(imageUrl,imageWidth,imageHeight)
        }
    }

    fun setView(imageUrl: String) {

        val displayMetrics = DisplayMetrics()
        val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val params = binding.photoView.layoutParams
        params.width = screenWidth
        //params.height = screenHeight
        binding.photoView.layoutParams = params
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = Glide.with(requireContext())
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