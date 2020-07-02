package social.entourage.android.entourage.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_create_entourage_description.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.category.EntourageCategoryFragment

/**
 * Dialog Fragment for editing an entourage entourageDescription
 */
class CreateEntourageDescriptionFragment  : EntourageDialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    //description_entourage_edittext)    var descriptionEditText: EditText? = null
    //description_entourage_info_text)    var infoTextView: TextView? = null
    private var entourageDescription: String? = null
    private var entourageCategory: EntourageCategory? = null
    private var entourageGroupType: String? = null
    private var mListener: CreateEntourageListener? = null
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            entourageDescription = args.getString(ENTOURAGE_DESCRIPTION)
            entourageCategory = args.getSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY) as EntourageCategory?
            entourageGroupType = args.getString(BaseCreateEntourageFragment.KEY_ENTOURAGE_GROUP_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_entourage_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        title_close_button?.setOnClickListener { onCloseClicked() }
        title_action_button?.setOnClickListener { onValidateClicked() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showKeyboard()
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
        description_entourage_edittext?.let {
            mListener?.onDescriptionChanged(it.text.toString())
        }
        mListener = null
        dismiss()
    }

    // ----------------------------------
    // Private Methods
    // ----------------------------------
    private fun initializeView() {
        entourageDescription?.let {
            description_entourage_edittext?.setText(it)
            description_entourage_edittext?.setSelection(it.length)
        }
        entourageCategory?.descriptionExample?.let{
            if (it.isNotEmpty()) {
                description_entourage_edittext?.hint = it
            }
        }
        if (BaseEntourage.GROUPTYPE_OUTING.equals(entourageGroupType, ignoreCase = true)) {
            description_entourage_edittext?.setHint(R.string.entourage_description_fragment_hint_outing)
            description_entourage_info_text?.setText(R.string.entourage_description_fragment_info_outing)
        }
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        val TAG = CreateEntourageDescriptionFragment::class.java.simpleName
        private const val ENTOURAGE_DESCRIPTION = "ENTOURAGE_DESCRIPTION"

        fun newInstance(description: String?, entourageCategory: EntourageCategory?, groupType: String?): CreateEntourageDescriptionFragment {
            val fragment = CreateEntourageDescriptionFragment()
            val args = Bundle()
            args.putString(ENTOURAGE_DESCRIPTION, description)
            args.putSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY, entourageCategory)
            args.putString(BaseCreateEntourageFragment.KEY_ENTOURAGE_GROUP_TYPE, groupType)
            fragment.arguments = args
            return fragment
        }
    }
}