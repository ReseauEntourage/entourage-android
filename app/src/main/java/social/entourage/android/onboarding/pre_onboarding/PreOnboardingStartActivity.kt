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
import kotlinx.android.synthetic.main.activity_intro_carousel.*
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber

class PreOnboardingStartActivity : AppCompatActivity() {

    var arrayViewDots = ArrayList<ImageView>()
    var currentDotPosition = 0

    lateinit var mAdapter: PreOnboardingRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_intro_carousel)

        arrayViewDots.add(ui_iv_dot1)
        arrayViewDots.add(ui_iv_dot2)
        arrayViewDots.add(ui_iv_dot3)

        setupViews()
        setupRecyclerView()
        updateViewAndDots()
    }

    private fun setupViews() {
        ui_button_connect?.setOnClickListener {
           goNext()
        }

        ui_button_next?.setOnClickListener {
            if (currentDotPosition < 2) {
                currentDotPosition += 1
                ui_recyclerView?.smoothScrollToPosition(currentDotPosition)
                updateViewAndDots()
                return@setOnClickListener
            }
            goNext()
        }

        ui_button_previous?.setOnClickListener {
                currentDotPosition -= 1
                ui_recyclerView?.smoothScrollToPosition(currentDotPosition)
                updateViewAndDots()
                return@setOnClickListener

        }
        ui_button_previous?.isVisible = false
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
        val datas = ArrayList<Int>()
        datas.add(R.drawable.carousel_onboarding_1)
        datas.add(R.drawable.carousel_onboarding_2)
        datas.add(R.drawable.carousel_onboarding_3)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        ui_recyclerView?.setHasFixedSize(true)
        ui_recyclerView?.layoutManager = linearLayoutManager
//        ui_recyclerView?.addItemDecoration(RecyclerViewItemDecorationCenterFirstLast(CELL_SPACING))

        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentDotPosition = linearLayoutManager.findFirstVisibleItemPosition()
                    updateViewAndDots()
                }
            }
        }

        ui_recyclerView?.addOnScrollListener(scrollListener)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(ui_recyclerView)

        linearLayoutManager.scrollToPosition(0)
        mAdapter = PreOnboardingRVAdapter(this, datas)
        ui_recyclerView?.adapter = mAdapter

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
        ui_logo?.visibility = View.GONE
        ui_button_connect?.visibility = View.VISIBLE
        ui_button_previous.isVisible = true
        when(currentDotPosition) {
            0 -> {
                title = getString(R.string.intro_title_1)
                description = getString(R.string.intro_subtitle_1)
                ui_logo?.visibility = View.VISIBLE
                ui_button_previous.isVisible = false
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
                ui_button_connect?.visibility = View.INVISIBLE
                AnalyticsEvents.logEvent(AnalyticsEvents.PreOnboard_car3)
            }
        }
        ui_tv_title?.text = title
        ui_tv_description?.text = description
    }
}