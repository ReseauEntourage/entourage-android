package social.entourage.android.about.carousel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import social.entourage.android.R
import social.entourage.android.base.EntourageDialogFragment
import java.util.*
import kotlinx.android.synthetic.main.fragment_carousel.*
import kotlinx.android.synthetic.main.fragment_carousel.view.*

/**
 * Help carousel
 */
class CarouselFragment  : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var dots: ArrayList<ImageView> =  ArrayList()

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_carousel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeCarousel()
    }

    private fun initializeCarousel() {
        carousel_view?.adapter = CarouselPageAdapter(childFragmentManager)
        carousel_view?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                selectDot(position)
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
        addDots()
        carousel_close_button?.setOnClickListener {dismiss()}
    }

    private fun addDots() {
        carousel_indicator_layout?.let {
            dots.add(it.carousel_b1)
            dots.add(it.carousel_b2)
            dots.add(it.carousel_b3)
            dots.add(it.carousel_b4)   
        }
    }

    fun selectDot(idx: Int) {
        for (i in 0 until CarouselPageAdapter.NUM_PAGES) {
            val drawableId = if (i == idx) R.drawable.carousel_bullet_filled else R.drawable.carousel_bullet_empty
            dots[i].setImageDrawable(AppCompatResources.getDrawable(requireContext(), drawableId))
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.about.carousel"
    }
}