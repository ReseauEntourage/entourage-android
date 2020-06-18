package social.entourage.android.invite.contacts

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import java.util.*

/**
 * Created by mihaiionescu on 28/07/16.
 *
 * Contacts Adapter with sections based on contact's first letter
 *
 */
class InviteContactsAdapter(private val context: Context, fromColumn: String) : BaseAdapter() {
    //private var cursor: Cursor? = null
    private val mFromColumn: String = fromColumn
    private val mData = ArrayList<InviteItem>()
    private val mSectionHeader = HashMap<String, Int>()
    private val mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItemViewType(position: Int): Int {
        return mData[position].itemType
    }

    override fun getViewTypeCount(): Int {
        return InviteItem.TYPE_COUNT
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(position: Int): InviteItem {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setItemSelected(position: Int, selected: Boolean) {
        getItem(position).isSelected = selected
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder = (convertView?.tag as ViewHolder?) ?: ViewHolder()
        val view = convertView ?:
            when (getItemViewType(position)) {
                InviteItem.TYPE_CONTACT_NAME -> {
                    mInflater.inflate(R.layout.layout_invite_contacts_list_name, null)
                            .also {
                                holder.textView = it.findViewById(R.id.contact_name)
                                holder.checkBox = null
                                holder.separator = it.findViewById(R.id.contact_separator)
                            } .apply { tag = holder }
                }
                InviteItem.TYPE_CONTACT_PHONE -> {
                    mInflater.inflate(R.layout.layout_invite_contacts_list_phone, null)
                            .also {
                                holder.textView = it.findViewById(R.id.contact_phone)
                                holder.checkBox = it.findViewById(R.id.contact_checkBox)
                                holder.separator = it.findViewById(R.id.contact_separator)
                            } .apply { tag = holder }
                }
                InviteItem.TYPE_SECTION -> {
                    mInflater.inflate(R.layout.layout_invite_contacts_section_header, null)
                            .also {
                                holder.textView = it.findViewById(R.id.contact_section_name)
                                holder.checkBox = null
                                holder.separator = null
                            } .apply { tag = holder }
                }
                else -> {//if no type found add a section...
                    mInflater.inflate(R.layout.layout_invite_contacts_section_header, null)
                            .also {
                                holder.textView = it.findViewById(R.id.contact_section_name)
                                holder.checkBox = null
                                holder.separator = null
                            } .apply { tag = holder }
                }
            }
        val item = mData[position]
        holder.textView?.text = item.itemText
        holder.checkBox?.let { it.isChecked = item.isSelected } ?: run {
            if (position < mData.size - 1) {
                //Hide the line separator if the next item is section type
                holder.separator?.visibility = if (mData[position + 1].itemType == InviteItem.TYPE_SECTION) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }
        return view
    }

    fun resetCursor() {
        mData.clear()
        mSectionHeader.clear()
        notifyDataSetInvalidated()
    }

    fun swapCursor(cursor: Cursor) {
        mData.clear()
        mSectionHeader.clear()
        val columnIndex = cursor.getColumnIndex(mFromColumn)
        if (columnIndex == -1) {
            return
        }
        val cr = context.contentResolver
        while (cursor.moveToNext()) {
            val cursorData = cursor.getString(columnIndex)
            val contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            val startChar = cursorData.substring(0, 1).toUpperCase(Locale.ENGLISH)
            if (!mSectionHeader.containsKey(startChar)) {
                mData.add(InviteItemSection(startChar))
                mSectionHeader[startChar] = mData.size - 1
            }
            mData.add(InviteItemContactName(cursorData, cursor.position))
            cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(contactID), null)
                    ?.let { pCur ->
                        val currentNumbers = ArrayList<String>()
                        while (pCur.moveToNext()) {
                            val contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            val stripNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                PhoneNumberUtils.formatNumber(contactNumber, "FR") ?: contactNumber
                            } else {
                                PhoneNumberUtils.stripSeparators(contactNumber)
                            }
                            if(currentNumbers.contains(stripNumber))
                                continue
                            val displayNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) stripNumber else contactNumber
                            mData.add(InviteItemContactPhone(displayNumber, cursor.position))
                            currentNumbers.add(stripNumber)
                        }
                        pCur.close()
                    }
        }
        notifyDataSetChanged()
    }

    fun getCursorPositionForItemAt(position: Int): Int {
        val item = mData[position]
        return if (item.itemType == InviteItem.TYPE_SECTION) {
            -1
        } else (item as InviteItemContactName).cursorPosition
    }

    fun getPositionForSection(jumpToString: String): Int {
        return if (mSectionHeader.containsKey(jumpToString)) {
            mSectionHeader[jumpToString] ?: -1
        } else -1
    }

    fun getPhoneAt(position: Int): String? {
        val item = mData[position]
        return if (item.itemType == InviteItem.TYPE_CONTACT_PHONE) {
            item.itemText
        } else null
    }

    class ViewHolder {
        var textView: TextView? = null
        var checkBox: CheckBox? = null
        var separator: View? = null
    }

}