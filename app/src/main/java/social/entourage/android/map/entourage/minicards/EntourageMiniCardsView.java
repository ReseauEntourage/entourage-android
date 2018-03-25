package social.entourage.android.map.entourage.minicards;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;

/**
 * Displays a list of entourage mini cards
 */
public class EntourageMiniCardsView extends RelativeLayout {

    RelativeLayout mainLayout;
    RecyclerView miniCardsRecyclerView;
    EntourageMiniCardsAdapter miniCardsAdapter;

    public EntourageMiniCardsView(Context context) {
        super(context);
        init(null, 0);
    }

    public EntourageMiniCardsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EntourageMiniCardsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.layout_entourage_mini_cards_view, this);

        mainLayout = (RelativeLayout) this.findViewById(R.id.mini_cards_view_rl);
        miniCardsRecyclerView = (RecyclerView) this.findViewById(R.id.mini_cards_view_recycler_view);

//        mainLayout.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                miniCardsRecyclerView.scrollToPosition(0);
//                miniCardsAdapter.removeAll();
//                EntourageMiniCardsView.this.setVisibility(INVISIBLE);
//            }
//        });

        initMiniCardsRecyclerView();
    }

    private void initMiniCardsRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        miniCardsRecyclerView.setLayoutManager(linearLayoutManager);
        miniCardsAdapter = new EntourageMiniCardsAdapter();
        miniCardsRecyclerView.setAdapter(miniCardsAdapter);
    }

    public void setEntourages(List<TimestampedObject> entourageList) {
        if (miniCardsAdapter == null) {
            return;
        }
        miniCardsRecyclerView.scrollToPosition(0);
        miniCardsAdapter.addItems(entourageList);
        this.setVisibility(VISIBLE);
    }

}
