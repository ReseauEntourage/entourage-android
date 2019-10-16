package social.entourage.android.invite.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
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
    private Context context;

    public InviteContactsAdapter(Context context, String fromColumn) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mFromColumn = fromColumn;
        this.context = context;
    }

    @Override
    public int getItemViewType(final int position) {
        return mData.get(position).getItemType();
    }

    @Override
    public int getViewTypeCount() {
        return InviteItem.TYPE_COUNT;
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

    public void setItemSelected(final int position, boolean selected) {
        InviteItem item = getItem(position);
        if (item != null) {
            item.setSelected(selected);
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case InviteItem.TYPE_CONTACT_NAME:
                    convertView = mInflater.inflate(R.layout.layout_invite_contacts_list_name, null);
                    holder.textView = convertView.findViewById(R.id.contact_name);
                    holder.checkBox = null;
                    holder.separator = convertView.findViewById(R.id.contact_separator);
                    break;
                case InviteItem.TYPE_CONTACT_PHONE:
                    convertView = mInflater.inflate(R.layout.layout_invite_contacts_list_phone, null);
                    holder.textView = convertView.findViewById(R.id.contact_phone);
                    holder.checkBox = convertView.findViewById(R.id.contact_checkBox);
                    holder.separator = convertView.findViewById(R.id.contact_separator);
                    break;
                case InviteItem.TYPE_SECTION:
                    convertView = mInflater.inflate(R.layout.layout_invite_contacts_section_header, null);
                    holder.textView = convertView.findViewById(R.id.contact_section_name);
                    holder.checkBox = null;
                    holder.separator = null;
                    break;
            }
            if (convertView != null) {
                convertView.setTag(holder);
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        InviteItem item = mData.get(position);
        if (item != null) {
            holder.textView.setText(item.getItemText());
            if (holder.checkBox != null) {
                holder.checkBox.setChecked(item.isSelected());
            }
        }
        if (holder.separator != null && position < mData.size()-1) {
            //Hide the line separator if the next item is section type
            if (mData.get(position+1).getItemType() == InviteItem.TYPE_SECTION) {
                holder.separator.setVisibility(View.GONE);
            } else {
                holder.separator.setVisibility(View.VISIBLE);
            }
        }

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

        ContentResolver cr = context.getContentResolver();

        while (mCursor.moveToNext()) {
            String cursorData = cursor.getString(columnIndex);
            String contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String startChar = cursorData.substring(0, 1).toUpperCase();
            if (!mSectionHeader.containsKey(startChar)) {
                mData.add(new InviteItemSection(startChar));
                mSectionHeader.put(startChar, mData.size() - 1);
            }
            mData.add(new InviteItemContactName(cursorData, mCursor.getPosition()));

            Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactID}, null);
            if (pCur != null) {
                while (pCur.moveToNext()) {
                    String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    mData.add(new InviteItemContactPhone(contactNumber, mCursor.getPosition()));
                }
                pCur.close();
            }

        }
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public int getCursorPositionForItemAt(int position) {
        InviteItem item = mData.get(position);
        if (item.getItemType() == InviteItem.TYPE_SECTION) {
            return -1;
        }
        return ((InviteItemContactName)item).getCursorPosition();
    }

    public int getPositionForSection(String jumpToString) {
        if (mSectionHeader.containsKey(jumpToString)) {
            return mSectionHeader.get(jumpToString);
        }
        return -1;
    }

    public String getPhoneAt(int position) {
        InviteItem item = mData.get(position);
        if (item.getItemType() == InviteItem.TYPE_CONTACT_PHONE) {
            return item.getItemText();
        }
        return null;
    }

    public static class ViewHolder {
        public TextView textView;
        public CheckBox checkBox;
        public View separator;
    }
}
