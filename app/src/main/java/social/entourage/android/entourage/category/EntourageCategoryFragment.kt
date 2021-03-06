package social.entourage.android.entourage.category

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import kotlinx.android.synthetic.main.fragment_entourage_category.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.Constants
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.EntLinkMovementMethod
import social.entourage.android.entourage.create.CreateEntourageListener

/**
 * A simple [BaseDialogFragment] subclass.
 * Activities that contain this fragment must implement the
 * [CreateEntourageListener] interface
 * to handle interaction events.
 * Use the [EntourageCategoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EntourageCategoryFragment : BaseDialogFragment() {
    private lateinit var category: EntourageCategory
    private var mListener: CreateEntourageListener? = null

    private lateinit var adapter: EntourageCategoriesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_entourage_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments == null) {
            dismiss()
            return
        }
        category = (requireArguments().getSerializable(KEY_ENTOURAGE_CATEGORY) as EntourageCategory?)
                ?:EntourageCategoryManager.defaultCategory
        if (!category.isNewlyCreated) {
            category.isSelected = true
        } else {
            category.isNewlyCreated = true
            category.isSelected = false
        }
        initializeView()
        title_close_button?.setOnClickListener {onCloseClicked()}
        title_action_button?.setOnClickListener {onValidateClicked()}
    }

    override fun onDismiss(dialog: DialogInterface) {
        resetSelectedCategory()
        super.onDismiss(dialog)
    }

    override fun dismiss() {
        resetSelectedCategory()
        super.dismiss()
    }

    private fun resetSelectedCategory() {
        // Reset the flag so consequent fragment shows will not appear broken
        adapter.selectedCategory.isSelected = false
    }

    fun setListener(mListener: CreateEntourageListener) {
        this.mListener = mListener
    }

    private fun initializeView() {
        initializeListView()
        initializeHelpHtmlView()
    }

    private fun initializeListView() {
        adapter = EntourageCategoriesAdapter(requireContext(),
                EntourageCategoryManager.entourageCategories,
                category) {
            //Return click
            onValidateClicked()
        }
        entourage_category_listview?.let {
            it.setAdapter(adapter)
            val count = adapter.groupCount
            for (position in 0 until count) {
                it.expandGroup(position)
            }
            //Disable click on group header
            it.setOnGroupClickListener { _: ExpandableListView?, _: View?, _: Int, _: Long -> true }
        }
    }

    private fun initializeHelpHtmlView() {
        (activity as? MainActivity)?.let { mainActivity ->
            val goalLink = mainActivity.getLink(Constants.GOAL_LINK_ID)
            entourage_category_help_link?.setHtmlString(getString(R.string.entourage_create_help_text, goalLink), EntLinkMovementMethod)
        }
    }

    // ----------------------------------
    // Interactions handling
    // ----------------------------------
    fun onCloseClicked() {
        mListener = null
        dismiss()
    }

    private fun onValidateClicked() {
        mListener?.onCategoryChosen(adapter.selectedCategory)
        mListener = null
        dismiss()
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        val TAG: String? = EntourageCategoryFragment::class.java.simpleName

        // ----------------------------------
        // Attributes
        // ----------------------------------
        const val KEY_ENTOURAGE_CATEGORY = "ENTOURAGE_CATEGORY"

        fun newInstance(category: EntourageCategory): EntourageCategoryFragment {
            val fragment = EntourageCategoryFragment()
            val args = Bundle()
            args.putSerializable(KEY_ENTOURAGE_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }
}