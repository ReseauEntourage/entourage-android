package social.entourage.android.authentication.login.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import kotlinx.android.synthetic.main.login_slideshow.*
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import java.util.*

/**
 * Login slideshow [Fragment] subclass.
 */
class LoginSlideshowFragment  : Fragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private val dots: ArrayList<ImageView> = ArrayList()
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.login_slideshow, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeCarousel()
    }

    private fun initializeCarousel() {
        login_slideshow_view?.let { pager ->
            pager.adapter = LoginSlideshowPageAdapter(childFragmentManager)
            pager.addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_LOGIN_SLIDESHOW)
                    selectDot(position)
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
        addDots()
    }

    private fun addDots() {
        login_slideshow_indicator_layout?.let { layout ->
            dots.clear()
            layout.findViewById<ImageView>(R.id.slideshow_b1)?.let { dots.add(it) }
            layout.findViewById<ImageView>(R.id.slideshow_b2)?.let { dots.add(it) }
            layout.findViewById<ImageView>(R.id.slideshow_b3)?.let { dots.add(it) }
            layout.findViewById<ImageView>(R.id.slideshow_b4)?.let { dots.add(it) }
        }
    }

    private fun selectDot(idx: Int) {
        if (dots.size <= idx) return
        for (i in 0 until LoginSlideshowPageAdapter.NUM_PAGES) {
            val drawableId = if (i == idx) R.drawable.carousel_bullet_filled else R.drawable.carousel_bullet_empty
            val drawable = AppCompatResources.getDrawable(requireContext(), drawableId)
            dots[i].setImageDrawable(drawable)
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG = LoginSlideshowFragment::class.java.simpleName
    }
}