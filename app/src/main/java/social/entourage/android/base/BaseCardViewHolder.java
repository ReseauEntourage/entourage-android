package social.entourage.android.base;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 02/03/16.
 */
abstract public class BaseCardViewHolder extends RecyclerView.ViewHolder {

    protected EntourageViewHolderListener viewHolderListener;

    public BaseCardViewHolder(View view) {
        super(view);
        bindFields();
    }

    public EntourageViewHolderListener getViewHolderListener() {
        return viewHolderListener;
    }

    public void setViewHolderListener(final EntourageViewHolderListener viewHolderListener) {
        this.viewHolderListener = viewHolderListener;
    }

    abstract protected void bindFields();

    abstract public void populate(@NotNull TimestampedObject data);

}
