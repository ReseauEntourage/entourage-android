package social.entourage.android.onboarding.pre_onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.databinding.ActivityIntroCarouselBinding
import social.entourage.android.tools.log.AnalyticsEvents

class PreOnboardingStartActivity : AppCompatActivity() {

    private var arrayViewDots = ArrayList<ImageView>()
    var currentDotPosition = 0

    private lateinit var mAdapter: PreOnboardingRVAdapter
    private lateinit var binding: ActivityIntroCarouselBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIntroCarouselBinding.inflate(layoutInflater)

        setContentView(binding.root)

        arrayViewDots.add(binding.uiIvDot1)
        arrayViewDots.add(binding.uiIvDot2)
        arrayViewDots.add(binding.uiIvDot3)

        setupViews()
        setupRecyclerView()
        updateViewAndDots()
    }

    private fun setupViews() {
        binding.uiButtonConnect.setOnClickListener {
           goNext()
        }

        binding.uiButtonNext.setOnClickListener {
            if (currentDotPosition < 2) {
                currentDotPosition += 1
                binding.uiRecyclerView.smoothScrollToPosition(currentDotPosition)
                updateViewAndDots()
                return@setOnClickListener
            }
            goNext()
        }

        binding.uiButtonPrevious.setOnClickListener {
                currentDotPosition -= 1
            binding.uiRecyclerView.smoothScrollToPosition(currentDotPosition)
                updateViewAndDots()
                return@setOnClickListener

        }
        binding.uiButtonPrevious.isVisible = false
    }

    private fun goNext() {

        if (EntourageApplication.get().authenticationController.isAuthenticated) {
            EntourageApplication.get().sharedPreferences.edit().putBoolean(EntourageApplication.KEY_MIGRATION_V7_OK,true).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        startActivity(Intent(this, PreOnboardingChoiceActivity::class.java))
        finish()
    }

    private fun setupRecyclerView() {
        val data = ArrayList<Int>()
        data.add(R.drawable.carousel_onboarding_1)
        data.add(R.drawable.carousel_onboarding_2)
        data.add(R.drawable.carousel_onboarding_3)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.uiRecyclerView.setHasFixedSize(true)
        binding.uiRecyclerView.layoutManager = linearLayoutManager
//        binding.uiRecyclerView?.addItemDecoration(RecyclerViewItemDecorationCenterFirstLast(CELL_SPACING))

        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentDotPosition = linearLayoutManager.findFirstVisibleItemPosition()
                    updateViewAndDots()
                }
            }
        }

        binding.uiRecyclerView.addOnScrollListener(scrollListener)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.uiRecyclerView)

        linearLayoutManager.scrollToPosition(0)
        mAdapter = PreOnboardingRVAdapter(this, data)
        binding.uiRecyclerView.adapter = mAdapter

        mAdapter.notifyDataSetChanged()

    }

    private fun updateViewAndDots() {
        for (i in 0 until arrayViewDots.size) {
            val drawableId = if (i == currentDotPosition) R.drawable.start_carousel_dot_selected else R.drawable.start_carousel_dot_unselected
            val drawable = AppCompatResources.getDrawable(this, drawableId)
            arrayViewDots[i].setImageDrawable(drawable)
        }

        val title: String
        val description: String
        binding.uiLogo.visibility = View.GONE
        binding.uiButtonConnect.visibility = View.VISIBLE
        binding.uiButtonPrevious.isVisible = true
        when(currentDotPosition) {
            0 -> {
                title = getString(R.string.intro_title_1)
                description = getString(R.string.intro_subtitle_1)
                binding.uiLogo.visibility = View.VISIBLE
                binding.uiButtonPrevious.isVisible = false
                AnalyticsEvents.logEvent(AnalyticsEvents.PreOnboard_car1)
            }
            1 -> {
                title = getString(R.string.intro_title_2)
                description = getString(R.string.intro_subtitle_2)
                AnalyticsEvents.logEvent(AnalyticsEvents.PreOnboard_car2)
            }
            else -> {
                title = getString(R.string.intro_title_3)
                description = getString(R.string.intro_subtitle_3)
                binding.uiButtonConnect.visibility = View.INVISIBLE
                AnalyticsEvents.logEvent(AnalyticsEvents.PreOnboard_car3)
            }
        }
        binding.uiTvTitle.text = title
        binding.uiTvDescription.text = description
    }
}