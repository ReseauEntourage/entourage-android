package social.entourage.android.onboarding.pre_onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_pre_onboarding_start.*
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.tools.Utils


class PreOnboardingStartActivity : AppCompatActivity() {

    val CELL_SPACING = 20
    var arrayViewDots = ArrayList<ImageView>()
    var currentDotPosition = 0

    lateinit var mAdapter: PreOnboardingRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_onboarding_start)

        arrayViewDots.add(ui_iv_dot1)
        arrayViewDots.add(ui_iv_dot2)
        arrayViewDots.add(ui_iv_dot3)
        arrayViewDots.add(ui_iv_dot4)

        setupViews()
        setupRecyclerView()
        updateViewAndDots()
    }

    private fun setupViews() {
        ui_button_connect?.setOnClickListener {
            startActivity(Intent(this, PreOnboardingChoiceActivity::class.java))
            finish()
        }

        ui_button_next?.setOnClickListener {
            if (currentDotPosition < 3) {
                currentDotPosition += 1
                ui_recyclerView?.smoothScrollToPosition(currentDotPosition)
                updateViewAndDots()
                return@setOnClickListener
            }
            startActivity(Intent(this, PreOnboardingChoiceActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        val datas = ArrayList<Int>()
        datas.add(R.drawable.pre_onboard_1)
        datas.add(R.drawable.pre_onboard_2)
        datas.add(R.drawable.pre_onboard_3)
        datas.add(R.drawable.pre_onboard_4)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        ui_recyclerView?.setHasFixedSize(true)
        ui_recyclerView?.layoutManager = linearLayoutManager
        ui_recyclerView?.addItemDecoration(RecyclerViewItemDecorationCenterFirstLast(CELL_SPACING))

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

        //Calculate padding and white view width
        val diff = (Utils.getScreenWidth(this) - Utils.convertDpToPixel(228.toFloat(),this)) / 2
        ui_fl_trans_left?.layoutParams?.width = (diff - CELL_SPACING).toInt()
        ui_fl_trans_right?.layoutParams?.width = (diff - CELL_SPACING).toInt()
        ui_recyclerView?.setPaddingRelative(diff.toInt(),0,diff.toInt(),0)
    }

    private fun updateViewAndDots() {
        for (i in 0 until arrayViewDots.size) {
            val drawableId = if (i == currentDotPosition) R.drawable.pre_onboard_dot_selected else R.drawable.pre_onboard_dot_unselected
            val drawable = AppCompatResources.getDrawable(this, drawableId)
            arrayViewDots[i].setImageDrawable(drawable)
        }

        val title: String
        val titleColored: String
        val color = ContextCompat.getColor(this,R.color.pre_onboard_orange)
        val description: String
        ui_iv_pre3?.visibility = View.GONE
        when(currentDotPosition) {
            0 -> {
                title = getString(R.string.pre_onboard_tutorial_title1)
                titleColored = getString(R.string.pre_onboard_tutorial_title1_colored)
                description = getString(R.string.pre_onboard_tutorial_description1)
                EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_START_CARROUSEL1)
            }
            1 -> {
                title = getString(R.string.pre_onboard_tutorial_title2)
                titleColored = getString(R.string.pre_onboard_tutorial_title2_colored)
                description = getString(R.string.pre_onboard_tutorial_description2)
                EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_START_CARROUSEL2)
            }
            2 -> {
                title = getString(R.string.pre_onboard_tutorial_title3)
                titleColored = getString(R.string.pre_onboard_tutorial_title3_colored)
                description = ""
                ui_iv_pre3?.visibility = View.VISIBLE
                EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_START_CARROUSEL3)
            }
            else -> {
                title = getString(R.string.pre_onboard_tutorial_title4)
                titleColored = getString(R.string.pre_onboard_tutorial_title4_colored)
                description = getString(R.string.pre_onboard_tutorial_description4)
                EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_START_CARROUSEL4)
            }
        }
        ui_tv_title?.text = Utils.formatTextWithBoldSpanAndColor(color,false,title,titleColored)
        ui_tv_description?.text = description
    }
}