package social.entourage.android.map.tour.information.discussion;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import social.entourage.android.base.BaseCardViewHolder;

/**
 * Created by mihaiionescu on 02/03/16.
 */
public class ViewHolderFactory {

    private HashMap<Integer, ViewHolderType> viewHolderTypeHashMap;

    public ViewHolderFactory() {
        viewHolderTypeHashMap = new HashMap<>();
    }

    public void registerViewHolder(int viewType, ViewHolderType viewHolderType) {
        viewHolderTypeHashMap.put(viewType, viewHolderType);
    }

    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        ViewHolderType viewHolderType = viewHolderTypeHashMap.get(viewType);
        if (viewHolderType == null) {
            return null;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(viewHolderType.layoutResource, parent, false);
        BaseCardViewHolder cardViewHolder = null;
        try {
            Constructor ctor;
            ctor = viewHolderType.cardViewHolderClass.getConstructor(View.class);
            cardViewHolder = (BaseCardViewHolder)ctor.newInstance(view);
        } catch (Exception e) {
            Log.d("TourInformation", "Invalid card!");
        }

        return cardViewHolder;
    }

    public static ViewHolderFactory createViewHolderFactory() {
        return new ViewHolderFactory();
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
