package social.entourage.android.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by mihaiionescu on 02/03/16.
 */
public class ViewHolderFactory {

    private HashMap<Integer, ViewHolderType> viewHolderTypeHashMap;
    private ViewHolderType viewHolderTypeDefault = null;

    ViewHolderFactory() {
        viewHolderTypeHashMap = new HashMap<>();
    }

    public void registerViewHolder(int viewType, ViewHolderType viewHolderType) {
        if(viewHolderTypeHashMap.isEmpty()) {
            viewHolderTypeDefault = viewHolderType;
        }
        viewHolderTypeHashMap.put(viewType, viewHolderType);
    }

    @NotNull
    public BaseCardViewHolder getViewHolder(ViewGroup parent, int viewType) {
        ViewHolderType viewHolderType = viewHolderTypeHashMap.get(viewType);
        if (viewHolderType == null) {
            viewHolderType = viewHolderTypeDefault;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(viewHolderType.layoutResource, parent, false);
        BaseCardViewHolder cardViewHolder = null;
        try {
            Constructor ctor = viewHolderType.cardViewHolderClass.getConstructor(View.class);
            cardViewHolder = (BaseCardViewHolder)ctor.newInstance(view);
        } catch (Exception e) {
            Timber.e(e);
        }

        return cardViewHolder;
    }

    public static class ViewHolderType {
        private int layoutResource;
        private Class cardViewHolderClass;

        public ViewHolderType(final Class cardViewHolderClass, final int layoutResource) {
            this.cardViewHolderClass = cardViewHolderClass;
            this.layoutResource = layoutResource;
        }
    }
}
