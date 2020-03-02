package social.entourage.android.entourage.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.entourage.fragment_create_entourage_join_type.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.R
import social.entourage.android.base.EntourageDialogFragment

/**
 * Choose entourage join type [EntourageDialogFragment] subclass.
 */
class CreateEntourageJoinTypeFragment : EntourageDialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var listener: CreateEntourageJoinTypeListener? = null

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_entourage_join_type, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title_close_button.setOnClickListener {dismiss()}
        entourage_join_type_option_private.setOnClickListener {onJoinTypePrivateClicked()}
        entourage_join_type_option_public.setOnClickListener {onJoinTypePublicClicked()}
    }

    fun setListener(listener: CreateEntourageJoinTypeListener?) {
        this.listener = listener
    }

    // ----------------------------------
    // Interface Handling
    // ----------------------------------
    private fun onJoinTypePrivateClicked() {
        listener?.createEntourageWithJoinTypePublic(false)
    }

    private fun onJoinTypePublicClicked() {
        listener?.createEntourageWithJoinTypePublic(true)
    }

    // ----------------------------------
    // Listener
    // ----------------------------------
    interface CreateEntourageJoinTypeListener {
        fun createEntourageWithJoinTypePublic(joinType: Boolean)
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        val TAG = CreateEntourageJoinTypeFragment::class.java.simpleName
    }
}