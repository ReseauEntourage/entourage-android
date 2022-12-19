package social.entourage.android.old_v7.entourage.minicards

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.layout_entourage_mini_cards_view.view.*
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage

/**
 * Displays a list of entourage mini cards
 */
class EntourageMiniCardsView : RelativeLayout {
    //var mainLayout: RelativeLayout? = null
    //var miniCardsRecyclerView: RecyclerView? = null
    private lateinit var miniCardsAdapter: EntourageMiniCardsAdapter

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        View.inflate(context, R.layout.layout_entourage_mini_cards_view, this)

//        mini_cards_view_rl.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                mini_cards_view_recycler_view.scrollToPosition(0);
//                miniCardsAdapter.removeAll();
//                EntourageMiniCardsView.this.setVisibility(INVISIBLE);
//            }
//        });
        initMiniCardsRecyclerView()
    }

    private fun initMiniCardsRecyclerView() {
        mini_cards_view_recycler_view?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        miniCardsAdapter = EntourageMiniCardsAdapter()
        mini_cards_view_recycler_view?.adapter = miniCardsAdapter
    }

    fun setEntourages(entourageList: List<BaseEntourage>) {
        mini_cards_view_recycler_view?.scrollToPosition(0)
        miniCardsAdapter.addItems(entourageList)
        this.visibility = View.VISIBLE
    }
}