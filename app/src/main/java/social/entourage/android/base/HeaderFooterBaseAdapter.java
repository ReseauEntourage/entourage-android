package social.entourage.android.base;

import social.entourage.android.api.model.TimestampedObject;

public class HeaderFooterBaseAdapter extends HeaderBaseAdapter {
    protected boolean showBottomView = false;
    protected int bottomViewContentType;

    protected void showBottomView(final boolean showBottomView, int bottomViewContentType) {
        this.showBottomView = showBottomView;
        this.bottomViewContentType = bottomViewContentType;
        if (items != null) {
            notifyItemChanged(getBottomViewPosition());
        }
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size() + getPositionOffset() + 1; // +1 for the footer
    }

    @Override
    public int getItemViewType(final int position) {
        if (position == getItemCount() - 1) {
            return TimestampedObject.BOTTOM_VIEW;
        }
        return super.getItemViewType(position);
    }

    private int getBottomViewPosition() {
        if (items != null) {
            return items.size() + getPositionOffset();
        }
        return 0;
    }

}
