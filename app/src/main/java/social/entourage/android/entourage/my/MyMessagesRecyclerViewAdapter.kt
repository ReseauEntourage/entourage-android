package social.entourage.android.entourage.my

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.base.newsfeed.FeedItemViewHolder
import social.entourage.android.entourage.EntourageViewHolder
import social.entourage.android.tools.LoaderCardViewHolder
import timber.log.Timber
import java.util.ArrayList

/**
 * Created by Jerome on 21/12/2021.
 */
class MyMessagesRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    val VIEW_LOADING = 0
    val VIEW_ITEM = 1

    val positionOffset = 1

    private var loaderCallback: LoadMoreCallback? = null

    var messages: ArrayList<BaseEntourage> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun updateDatas(messages:ArrayList<BaseEntourage>) {
        this.messages = messages
        notifyDataSetChanged()
    }

    fun setLoaderCallback(loaderCallback: LoadMoreCallback?) {
        this.loaderCallback = loaderCallback
    }

    override fun getItemViewType(position: Int): Int {
        if (messages[position].category == LOADER_CARD) return VIEW_LOADING
        return VIEW_ITEM
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == VIEW_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_my_messages, parent, false)

            return MyMessagesViewHolder(view)
        }
        else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_loader_card, parent, false)

            return LoaderCardViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_ITEM) {
            (holder as? MyMessagesViewHolder)?.populate(messages[position])
        }
        else {
            (holder as? LoaderCardViewHolder)?.populate(messages[position])
        }

        if ((position >= positionOffset) && (messages[position].category == LOADER_CARD)) {
            loaderCallback?.loadMoreItems()
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    companion object {
        val LOADER_CARD = "loading"
    }

    interface LoadMoreCallback {
        fun loadMoreItems()
    }
}