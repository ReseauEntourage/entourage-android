package social.entourage.android.small_talks

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import social.entourage.android.R
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.GroupMember
import social.entourage.android.api.model.User
import social.entourage.android.api.model.toUsers
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivitySmallTalkGroupFoundBinding
import social.entourage.android.discussions.DiscussionsPresenter

class SmallTalkGroupFoundActivity : BaseActivity() {

    private lateinit var binding: ActivitySmallTalkGroupFoundBinding
    private lateinit var adapter: SmallTalkGroupFoundAdapter
    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmallTalkGroupFoundBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        discussionsPresenter.detailConversation.observe(this) {
            handleDetailConversation(it)
        }

        // TODO: bouton "Discuter"
        binding.buttonStart.setOnClickListener {
            // TODO: action
        }
    }

    override fun onResume() {
        super.onResume()
        discussionsPresenter.getDetailConversation(136082)
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

                // distance caméra plus petite pour effet coverflow plus subtil
                page.cameraDistance = 10000f

                if (position in -1f..1f) {
                    page.alpha = 1f

                    // Rotation plus douce
                    page.rotationY = position * -25f

                    // Zoom plus marqué au centre
                    val scale = 0.85f + (1 - absPos) * 0.15f
                    page.scaleX = scale
                    page.scaleY = scale

                    // Réduit la translation latérale = cartes plus serrées
                    page.translationX = -position * page.width * 0.15f
                } else {
                    page.alpha = 0f
                }
            }
        }
    }

    private fun handleDetailConversation(conversation: Conversation?) {
        val users = conversation?.members?.toUsers() ?: emptyList()
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

    private fun updateDots(currentIndex: Int, total: Int) {
        binding.smallTalkGroupFoundDotsContainer.removeAllViews()
        repeat(total) { index ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    if (index == currentIndex) 24 else 16,
                    16
                ).apply {
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
}
