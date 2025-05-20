package social.entourage.android.small_talks

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.api.model.toUsers
import social.entourage.android.base.BaseActivity
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.databinding.ActivitySmallTalkGroupFoundBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class SmallTalkGroupFoundActivity : BaseActivity() {

    private lateinit var binding: ActivitySmallTalkGroupFoundBinding
    private lateinit var adapter: SmallTalkGroupFoundAdapter
    private val smallTalkViewModel: SmallTalkViewModel by viewModels()

    private var smallTalkId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmallTalkGroupFoundBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updatePaddingTopForEdgeToEdge(binding.root)

        smallTalkId = intent.getIntExtra(EXTRA_SMALL_TALK_ID, -1)

        setupViewPager()
        smallTalkViewModel.smallTalkDetail.observe(this) { smallTalk ->
            val currentUserId = EntourageApplication.me(this)?.id
            val users = smallTalk?.members
                ?.toUsers()
                ?.filter { it.id != currentUserId } ?: emptyList()
            adapter = SmallTalkGroupFoundAdapter(users)
            binding.smallTalkGroupFoundViewpager.adapter = adapter
            updateDots(0, users.size)

            binding.smallTalkGroupFoundViewpager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateDots(position, users.size)
                }
            })
        }

        smallTalkViewModel.getSmallTalk(smallTalkId.toString())

        binding.buttonStart.setOnClickListener {
            val intent = Intent(this, DetailConversationActivity::class.java)
            DetailConversationActivity.isSmallTalkMode = true
            DetailConversationActivity.smallTalkId = smallTalkId.toString()
            startActivity(intent)
            finish()
        }
    }

    private fun setupViewPager() {
        val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.page_margin)
        val offsetPx = resources.getDimensionPixelOffset(R.dimen.offset)

        binding.smallTalkGroupFoundViewpager.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPadding(offsetPx, 0, offsetPx, 0)

            setPageTransformer { page, position ->
                val absPos = kotlin.math.abs(position)
                page.cameraDistance = 10000f
                if (position in -1f..1f) {
                    page.alpha = 1f
                    page.rotationY = position * -25f
                    val scale = 0.85f + (1 - absPos) * 0.15f
                    page.scaleX = scale
                    page.scaleY = scale
                    page.translationX = -position * page.width * 0.15f
                } else {
                    page.alpha = 0f
                }
            }
        }
    }

    private fun updateDots(currentIndex: Int, total: Int) {
        binding.smallTalkGroupFoundDotsContainer.removeAllViews()
        repeat(total) { index ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(16, 16).apply {
                    setMargins(8, 0, 8, 0)
                }
                background = ContextCompat.getDrawable(
                    this@SmallTalkGroupFoundActivity,
                    if (index == currentIndex) R.drawable.dot_selected else R.drawable.dot_unselected
                )
            }
            binding.smallTalkGroupFoundDotsContainer.addView(dot)
        }
    }

    companion object {
        const val EXTRA_SMALL_TALK_ID = "extra_small_talk_id"
    }
}
