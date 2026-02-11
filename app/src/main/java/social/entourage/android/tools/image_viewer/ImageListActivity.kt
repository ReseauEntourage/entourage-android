package social.entourage.android.tools.image_viewer

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
import social.entourage.android.api.model.ConversationImageSingleWrapper
import social.entourage.android.api.model.ConversationImagesWrapper
import social.entourage.android.comment.ImageZoomActivity
import social.entourage.android.tools.utils.Const

class ImageListActivity : AppCompatActivity(), ImageGridAdapter.OnImageClickListener {

    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var backBtn: ImageView
    private lateinit var adapter: ImageGridAdapter

    private var conversationId: Int = -1

    // Pagination state
    private val images = mutableListOf<ConversationImage>()
    private var currentPage = 1
    private val perPage = 40
    private var isLoadingPage = false
    private var isLastPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_list)

        recycler = findViewById(R.id.recycler_images)
        progress = findViewById(R.id.progress)
        emptyView = findViewById(R.id.empty_view)
        backBtn = findViewById(R.id.header_icon_back)

        conversationId = intent.getIntExtra(Const.CONVERSATION_ID, -1)

        val span = 3
        val layoutManager = GridLayoutManager(this, span)
        recycler.layoutManager = layoutManager
        recycler.addItemDecoration(
            GridSpacingItemDecoration(span, resources.getDimensionPixelSize(R.dimen.grid_spacing_8))
        )
        adapter = ImageGridAdapter(emptyList(), this)
        recycler.adapter = adapter

        // Scroll listener pour déclencher le chargement de la page suivante
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                if (dy <= 0) return
                if (isLoadingPage || isLastPage) return

                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount

                // Déclenche le chargement quand on approche de la fin
                val threshold = 6
                if (lastVisible >= total - threshold) {
                    loadNextPage()
                }
            }
        })

        backBtn.setOnClickListener { finish() }

        if (conversationId != -1) {
            loadFirstPage()
        } else {
            emptyView.visibility = View.VISIBLE
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progress.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun loadFirstPage() {
        // reset complet
        currentPage = 1
        isLastPage = false
        images.clear()
        adapter.submitList(images.toList())
        emptyView.visibility = View.GONE

        setLoading(true)
        isLoadingPage = true

        EntourageApplication.get().apiModule.discussionsRequest
            .getConversationImages(conversationId, currentPage, perPage)
            .enqueue(object : Callback<ConversationImagesWrapper> {
                override fun onResponse(
                    call: Call<ConversationImagesWrapper>,
                    response: Response<ConversationImagesWrapper>
                ) {
                    setLoading(false)
                    isLoadingPage = false

                    val list = response.body()?.images.orEmpty()
                    images.addAll(list)
                    adapter.submitList(images.toList())

                    // Empty state si 0 résultat
                    emptyView.visibility = if (images.isEmpty()) View.VISIBLE else View.GONE

                    // Marque fin si moins que perPage
                    if (list.size < perPage) {
                        isLastPage = true
                    } else {
                        currentPage += 1
                    }
                }

                override fun onFailure(call: Call<ConversationImagesWrapper>, t: Throwable) {
                    setLoading(false)
                    isLoadingPage = false
                    if (images.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                    }
                }
            })
    }

    private fun loadNextPage() {
        if (isLoadingPage || isLastPage) return
        isLoadingPage = true
        // Pour la pagination, on ne bloque pas toute la vue : pas de progress global
        EntourageApplication.get().apiModule.discussionsRequest
            .getConversationImages(conversationId, currentPage, perPage)
            .enqueue(object : Callback<ConversationImagesWrapper> {
                override fun onResponse(
                    call: Call<ConversationImagesWrapper>,
                    response: Response<ConversationImagesWrapper>
                ) {
                    isLoadingPage = false
                    val list = response.body()?.images.orEmpty()

                    if (list.isNotEmpty()) {
                        images.addAll(list)
                        adapter.submitList(images.toList())
                    }

                    if (list.size < perPage) {
                        isLastPage = true
                    } else {
                        currentPage += 1
                    }
                }

                override fun onFailure(call: Call<ConversationImagesWrapper>, t: Throwable) {
                    isLoadingPage = false
                    // pas d'UI particulière, on laisse l'utilisateur rescroller pour retenter
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
