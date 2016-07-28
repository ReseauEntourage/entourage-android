package social.entourage.android.invite.contacts;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import social.entourage.android.R;

/**
 * Created by mihaiionescu on 28/07/16.
 *
 * Contacts Adapter with sections based on contact's first letter
 *
 */
public class InviteContactsAdapter extends BaseAdapter {

    private Cursor mCursor;
    private String mFromColumn;
    private ArrayList<InviteItem> mData = new ArrayList<>();
    private HashMap<String, Integer> mSectionHeader = new HashMap<>();

    private LayoutInflater mInflater;

    public InviteContactsAdapter(Context context, String fromColumn) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mFromColumn = fromColumn;
    }

    @Override
    public int getItemViewType(final int position) {
        return mData.get(position).getItemType();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public InviteItem getItem(final int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case InviteItem.TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.layout_invite_contacts_list_item, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.contact_name);
                    break;
                case InviteItem.TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.layout_invite_contacts_section_header, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.contact_section_name);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(mData.get(position).getItemText());

        return convertView;
    }

    public void swapCursor(Cursor cursor) {
        mData = new ArrayList<>();
        mSectionHeader = new HashMap<>();
        mCursor = cursor;
        if (cursor == null) {
            notifyDataSetInvalidated();
            return;
        }
        int columnIndex = mCursor.getColumnIndex(mFromColumn);
        if (columnIndex == -1) {
            return;
        }
        while (mCursor.moveToNext()) {
            String cursorData = cursor.getString(columnIndex);
            String startChar = cursorData.substring(0, 1).toUpperCase();
            if (!mSectionHeader.containsKey(startChar)) {
                mData.add(new InviteItemSection(startChar));
                mSectionHeader.put(startChar, mData.size()-1);
            }
            mData.add(new InviteItemContact(cursorData, mCursor.getPosition()));
        }
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public int getCursorPositionForItemAt(int position) {
        InviteItem item = mData.get(position);
        if (item.getItemType() == InviteItem.TYPE_SEPARATOR) {
            return -1;
        }
        return ((InviteItemContact)item).getCursorPosition();
    }

    public int getPositionForSection(String jumpToString) {
        if (mSectionHeader.containsKey(jumpToString)) {
            return mSectionHeader.get(jumpToString);
        }
        return -1;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}
