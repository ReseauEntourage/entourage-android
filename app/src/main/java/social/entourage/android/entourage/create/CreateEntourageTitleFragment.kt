package social.entourage.android.entourage.create

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.fragment_create_entourage_title.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.category.EntourageCategoryFragment

/**
 * Dialog Fragment for editing the entourage title
 */
class CreateEntourageTitleFragment  : EntourageDialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var entourageTitle: String? = null
    private var entourageCategory: EntourageCategory? = null
    private var entourageGroupType: String? = null
    private var mListener: CreateEntourageListener? = null
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {args ->
            entourageTitle = args.getString(KEY_ENTOURAGE_TITLE)
            entourageCategory = args.getSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY) as EntourageCategory?
            entourageGroupType = args.getString(BaseCreateEntourageFragment.KEY_ENTOURAGE_GROUP_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_entourage_title, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title_close_button?.setOnClickListener { onCloseClicked() }
        title_action_button?.setOnClickListener { onValidateClicked() }
        initializeView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showKeyboard()
    }

    override fun getSlideStyle(): Int {
        return R.style.CustomDialogFragmentFromRight
    }

    fun setListener(mListener: CreateEntourageListener?) {
        this.mListener = mListener
    }

    // ----------------------------------
    // Interactions handling
    // ----------------------------------
    fun onCloseClicked() {
        mListener = null
        dismiss()
    }

    fun onValidateClicked() {
        title_entourage_edittext?.let { mListener?.onTitleChanged(it.text.toString()) }
        mListener = null
        dismiss()
    }

    // ----------------------------------
    // Private Methods
    // ----------------------------------
    private fun initializeView() {
        title_entourage_edittext?.let {
            entourageTitle?.let { title ->
                it.setText(title)
                it.setSelection(title.length.coerceAtMost(TITLE_MAX_CHAR_COUNT))
            }
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    val charCountString = context!!.getString(R.string.entourage_create_title_char_count_format, s.length, TITLE_MAX_CHAR_COUNT)
                    title_entourage_count?.text = charCountString
                    if (s.length >= TITLE_MAX_CHAR_COUNT) {
                        title_entourage_count?.setTextColor(ResourcesCompat.getColor(resources, R.color.entourage_error, null))
                        title_entourage_info?.visibility = View.GONE
                        title_entourage_error?.visibility = View.VISIBLE
                    } else {
                        title_entourage_count?.setTextColor(ResourcesCompat.getColor(resources, R.color.entourage_ok, null))
                        title_entourage_info?.visibility = View.VISIBLE
                        title_entourage_error?.visibility = View.GONE
                    }
                }
            })
            title_entourage_count?.text = getString(R.string.entourage_create_title_char_count_format, it.length(), TITLE_MAX_CHAR_COUNT)
            entourageCategory?.titleExample?.let {example ->
                if (example.isNotEmpty()) {
                    it.hint = getString(R.string.entourage_create_title_hint, example)
                }
            }
            if (BaseEntourage.GROUPTYPE_OUTING.equals(entourageGroupType, ignoreCase = true)) {
                it.setHint(R.string.entourage_title_fragment_hint_outing)
                title_entourage_info_text?.setText(R.string.entourage_title_fragment_info_outing)
            }
        }
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        @JvmField
        val TAG = CreateEntourageTitleFragment::class.java.simpleName
        private const val KEY_ENTOURAGE_TITLE = "KEY_ENTOURAGE_TITLE"
        private const val TITLE_MAX_CHAR_COUNT = 100
        @JvmStatic
        fun newInstance(title: String?, entourageCategory: EntourageCategory?, groupType: String?): CreateEntourageTitleFragment {
            val fragment = CreateEntourageTitleFragment()
            val args = Bundle()
            args.putString(KEY_ENTOURAGE_TITLE, title)
            args.putSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY, entourageCategory)
            args.putString(BaseCreateEntourageFragment.KEY_ENTOURAGE_GROUP_TYPE, groupType)
            fragment.arguments = args
            return fragment
        }
    }
}