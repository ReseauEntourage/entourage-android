package social.entourage.android.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

/**
 * Map Tab View
 */
public class MapTabView extends FrameLayout {

    @BindView(R.id.map_tab_all_button)
    RadioButton allButton;

    @BindView(R.id.map_tab_events_button)
    RadioButton eventsButton;

    public MapTabView(Context context) {
        super(context);
        init(null, 0);
    }

    public MapTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MapTabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        View view = inflate(getContext(), R.layout.layout_map_tab, this);

        ButterKnife.bind(this, view);

        // Load attributes
    }

    @OnClick(R.id.map_tab_all_button)
    protected void onAllButtonClicked() {
        eventsButton.setChecked(false);
        BusProvider.getInstance().post(new Events.OnMapTabSelected(MapTabItem.ALL_TAB));
    }

    @OnClick(R.id.map_tab_events_button)
    protected void onEventsButtonClicked() {
        allButton.setChecked(false);
        BusProvider.getInstance().post(new Events.OnMapTabSelected(MapTabItem.EVENTS_TAB));
    }
}
