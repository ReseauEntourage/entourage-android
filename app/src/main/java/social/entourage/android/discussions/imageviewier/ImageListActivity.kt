package social.entourage.android.discussions.imageviewier

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.ConversationImage
import social.entourage.android.api.model.ConversationImagesWrapper
import social.entourage.android.api.model.ConversationImageSingleWrapper
import social.entourage.android.comment.ImageZoomActivity

class ImageListActivity : AppCompatActivity(), ImageGridAdapter.OnImageClickListener {

    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var backBtn: ImageView
    private lateinit var adapter: ImageGridAdapter
    private var conversationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_list)

        recycler = findViewById(R.id.recycler_images)
        progress = findViewById(R.id.progress)
        emptyView = findViewById(R.id.empty_view)
        backBtn = findViewById(R.id.header_icon_back)

        conversationId = intent.getIntExtra("conversation_id", -1)

        val span = 3
        recycler.layoutManager = GridLayoutManager(this, span)
        recycler.addItemDecoration(
            GridSpacingItemDecoration(span, resources.getDimensionPixelSize(R.dimen.grid_spacing_8))
        )
        adapter = ImageGridAdapter(emptyList(), this)
        recycler.adapter = adapter

        backBtn.setOnClickListener { finish() }

        if (conversationId != -1) {
            loadThumbnails()
        } else {
            emptyView.visibility = View.VISIBLE
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progress.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun loadThumbnails() {
        setLoading(true)
        EntourageApplication.get().apiModule.discussionsRequest
            .getConversationImages(conversationId)
            .enqueue(object : Callback<ConversationImagesWrapper> {
                override fun onResponse(
                    call: Call<ConversationImagesWrapper>,
                    response: Response<ConversationImagesWrapper>
                ) {
                    setLoading(false)
                    val list: List<ConversationImage> = response.body()?.images.orEmpty()
                    emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    adapter.submitList(list)
                }

                override fun onFailure(call: Call<ConversationImagesWrapper>, t: Throwable) {
                    setLoading(false)
                    emptyView.visibility = View.VISIBLE
                }
            })
    }

    override fun onImageClicked(chatMessageId: Int) {
        if (conversationId == -1) return
        setLoading(true)
        EntourageApplication.get().apiModule.discussionsRequest
            .getConversationImage(conversationId, chatMessageId)
            .enqueue(object : Callback<ConversationImageSingleWrapper> {
                override fun onResponse(
                    call: Call<ConversationImageSingleWrapper>,
                    response: Response<ConversationImageSingleWrapper>
                ) {
                    setLoading(false)
                    val url = response.body()?.image?.url
                    if (!url.isNullOrBlank()) {
                        val intent = Intent(this@ImageListActivity, ImageZoomActivity::class.java)
                        intent.putExtra("image_url", url)
                        startActivity(intent)
                    }
                }

                override fun onFailure(call: Call<ConversationImageSingleWrapper>, t: Throwable) {
                    setLoading(false)
                }
            })
    }
}
