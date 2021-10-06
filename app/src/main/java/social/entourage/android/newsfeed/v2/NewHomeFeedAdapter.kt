package social.entourage.android.newsfeed.v2

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_cell_home_action.view.*
import kotlinx.android.synthetic.main.layout_cell_home_empty.view.*
import kotlinx.android.synthetic.main.layout_cell_home_event.view.*
import kotlinx.android.synthetic.main.layout_cell_home_headline.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.tools.view.RecyclerViewItemDecorationWithSpacing

/**
 * NewHomeFeedAdapter.
 */
class NewHomeFeedAdapter(val listener:HomeViewHolderListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun updateDatas(items:ArrayList<HomeCard>,isNeighbour:Boolean,isLoading:Boolean) {
        this.isNeighbour = isNeighbour
        this.arrayItems.clear()
        this.arrayItems.addAll(items)
        this.isLoading = isLoading
        notifyDataSetChanged()
    }
    var arrayItems = ArrayList<HomeCard>()
    val CELL_HEADLINES = 0
    val CELL_ACTIONS = 1
    val CELL_EVENTS = 2
    val CELL_INFO = 3
    var isNeighbour = false
    var isLoading = false

    override fun getItemViewType(position: Int): Int {
        if (isNeighbour) {
            if (position == arrayItems.size) return CELL_INFO
        }
       val type = arrayItems[position].type

        when(type) {
            HomeCardType.HEADLINES -> return CELL_HEADLINES
            HomeCardType.ACTIONS -> return CELL_ACTIONS
            else -> return CELL_EVENTS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        when(viewType) {
            CELL_HEADLINES -> {
                val view = inflater.inflate(R.layout.layout_cell_home_headline, parent, false)
                return HeadlineVH(view)
            }
            CELL_ACTIONS -> {
                val view = inflater.inflate(R.layout.layout_cell_home_action, parent, false)
                return  ActionVH(view)
            }
            CELL_EVENTS -> {
                val view = inflater.inflate(R.layout.layout_cell_home_event, parent, false)
                return  EventVH(view)
            }
            CELL_INFO -> {
                val view = inflater.inflate(R.layout.layout_cell_home_info, parent, false)
                return  InfoVH(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.layout_cell_home_empty, parent, false)
                return EmptyVH(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            CELL_HEADLINES -> {
                if (holder is HeadlineVH) {
                    holder.bind(EntourageApplication.get(), position)
                }
            }
           CELL_ACTIONS -> {
                if (holder is ActionVH) {
                    holder.bind(EntourageApplication.get(), position)
                }
            }
            CELL_EVENTS -> {
                if (holder is EventVH) {
                    holder.bind(EntourageApplication.get(), position)
                }
            }
            CELL_INFO -> {
                if (holder is InfoVH) {
                    holder.bind()
                }
            }
            else-> {
                if (holder is EmptyVH) {
                    holder.bind(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        if (isNeighbour && !isLoading) return arrayItems.size + 1

        return arrayItems.size
    }

    inner class HeadlineVH(view: View) : RecyclerView.ViewHolder(view) {
        var itemDecoration:RecyclerView.ItemDecoration? = null

        fun bind(context: Context,position: Int) {
            if (isLoading) {
                itemView.ui_title_headline.text = "********** ***"
                itemView.ui_title_headline.setTextColor(ResourcesCompat.getColor(context.resources,R.color.background_grey_empty,null))
                itemView.ui_title_headline.setBackgroundColor(ResourcesCompat.getColor(context.resources,R.color.background_grey_empty,null))
            }
            else {
                itemView.ui_title_headline.text = context.getText(arrayItems[position].type.getName())
                itemView.ui_title_headline.setTextColor(ResourcesCompat.getColor(context.resources,R.color.black,null))
                itemView.ui_title_headline.setBackgroundColor(ResourcesCompat.getColor(context.resources,R.color.transparent,null))
            }


            itemView.ui_recyclerview_headline?.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            val adapter = HeadlineAdapter(arrayItems[position],listener,isLoading)
            itemView.ui_recyclerview_headline?.adapter = adapter

            itemDecoration?.let { itemView.ui_recyclerview_headline?.removeItemDecoration(it) }
            itemDecoration = RecyclerViewItemDecorationWithSpacing(5,24, Resources.getSystem())
            itemDecoration?.let { itemView.ui_recyclerview_headline?.addItemDecoration(it) }
        }
    }

    inner class ActionVH(view: View) : RecyclerView.ViewHolder(view) {
        var itemDecoration:RecyclerView.ItemDecoration? = null

        fun bind(context: Context, position: Int) {
            if (isLoading) {
                itemView.ui_title_action.text = "********** ***"
                itemView.ui_title_action.setTextColor(ResourcesCompat.getColor(context.resources,R.color.background_grey_empty,null))
                itemView.ui_title_action.setBackgroundColor(ResourcesCompat.getColor(context.resources,R.color.background_grey_empty,null))
                itemView.ui_action_show_more.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.ic_home_arrow_show_grey,null))
            }
            else {
                itemView.ui_title_action.setTextColor(ResourcesCompat.getColor(context.resources,R.color.black,null))
                itemView.ui_title_action.setBackgroundColor(ResourcesCompat.getColor(context.resources,R.color.transparent,null))
                itemView.ui_action_show_more.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.button_arrow_next_black_state,null))

                if (arrayItems[position].subtype == HomeCardType.ACTIONS_CONTRIB || arrayItems[position].subtype == HomeCardType.ACTIONS_ASK ) {
                    itemView.ui_title_action.text = context.getText(arrayItems[position].subtype.getName())
                }
                else {
                    itemView.ui_title_action.text = context.getText(arrayItems[position].type.getName())
                }
            }

            itemView.ui_action_show_more.setOnClickListener {
                listener.onShowDetail(arrayItems[position].type,true,arrayItems[position].subtype)
            }
            itemView.ui_action_show_more_txt.setOnClickListener {
                listener.onShowDetail(arrayItems[position].type,true,arrayItems[position].subtype)
            }

            itemView.ui_recyclerview_action?.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            val adapter = ActionEventAdapter(arrayItems[position],listener,isLoading)
            itemView.ui_recyclerview_action?.adapter = adapter

            itemDecoration?.let { itemView.ui_recyclerview_action?.removeItemDecoration(it) }
            itemDecoration = RecyclerViewItemDecorationWithSpacing(5,24, Resources.getSystem())
            itemDecoration?.let { itemView.ui_recyclerview_action?.addItemDecoration(it) }
        }
    }

    inner class EventVH(view: View) : RecyclerView.ViewHolder(view) {
        var itemDecoration:RecyclerView.ItemDecoration? = null

        fun bind(context: Context, position: Int) {

            if (isLoading) {
                itemView.ui_title_event.text = "********** ***"
                itemView.ui_title_event.setTextColor(ResourcesCompat.getColor(context.resources,R.color.background_grey_empty,null))
                itemView.ui_title_event.setBackgroundColor(ResourcesCompat.getColor(context.resources,R.color.background_grey_empty,null))
                itemView.ui_event_show_more.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.ic_home_arrow_show_grey,null))
            }
            else {
                itemView.ui_title_event.setTextColor(ResourcesCompat.getColor(context.resources, R.color.black, null))
                itemView.ui_title_event.setBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.transparent, null))
                itemView.ui_event_show_more.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.button_arrow_next_black_state, null))
                itemView.ui_title_event.text = context.getText(arrayItems[position].type.getName())
            }

                itemView.ui_event_show_more.setOnClickListener {
                listener.onShowDetail(arrayItems[position].type,true,arrayItems[position].subtype)
            }
            itemView.ui_event_show_more_txt.setOnClickListener {
                listener.onShowDetail(arrayItems[position].type,true,arrayItems[position].subtype)
            }

            itemView.ui_recyclerview_event?.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            val adapter = ActionEventAdapter(arrayItems[position],listener,isLoading)
            itemView.ui_recyclerview_event?.adapter = adapter

            itemDecoration?.let { itemView.ui_recyclerview_event?.removeItemDecoration(it) }
            itemDecoration = RecyclerViewItemDecorationWithSpacing(5,24, Resources.getSystem())
            itemDecoration?.let { itemView.ui_recyclerview_event?.addItemDecoration(it) }
        }
    }

    inner class InfoVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            itemView.setOnClickListener {
                listener.onShowChangeMode()
            }
        }
    }

    inner class EmptyVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(position: Int) {
            itemView.ui_title_empty.text = itemView.resources.getText(arrayItems[position].type.getName())

            itemView.ui_empty_show_more.setOnClickListener {
                listener.onShowDetail(arrayItems[position].type,true,arrayItems[position].subtype)
            }
            itemView.ui_empty_show_more_txt.setOnClickListener {
                listener.onShowDetail(arrayItems[position].type,true,arrayItems[position].subtype)
            }
        }
    }
}

