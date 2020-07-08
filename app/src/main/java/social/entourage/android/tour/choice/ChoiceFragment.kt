package social.entourage.android.tour.choice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import kotlinx.android.synthetic.main.fragment_choice.*
import social.entourage.android.R
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.tour.Tour.TourComparatorNewToOld
import social.entourage.android.api.model.tour.Tour.Tours
import social.entourage.android.tour.choice.ChoiceAdapter.RecyclerViewClickListener
import java.util.*

class ChoiceFragment : DialogFragment(), RecyclerViewClickListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private val tours: List<Tour> by lazy {
        (requireArguments().getSerializable(Tour.KEY_TOURS) as Tours?)?.tours ?: ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val toReturn = inflater.inflate(R.layout.fragment_choice, container, false)
        Collections.sort(tours, TourComparatorNewToOld())
        initializeView()
        return toReturn
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.CustomDialogFragmentFade
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun initializeView() {
        choice_recycler_view?.let {
            it.addItemDecoration(object : ItemDecoration() {})
            it.layoutManager = LinearLayoutManager(activity)
            it.adapter = ChoiceAdapter(this, tours)
            it.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (it.childCount > 0) {
                        val lastCell = it.getChildAt(it.childCount - 1)
                        if (lastCell.bottom < it.height) {
                            val layoutParams = it.layoutParams
                            layoutParams.height = lastCell.bottom
                            it.layoutParams = layoutParams
                        }
                    }
                }
            })
        }
    }

    private val onChoiceFragmentFinish: OnChoiceFragmentFinish?
        get() = activity as? OnChoiceFragmentFinish?

    override fun recyclerViewListClicked(tour: Tour) {
        onChoiceFragmentFinish?.closeChoiceFragment(this, tour)
    }

    // ----------------------------------
    // INNER CLASSE
    // ----------------------------------
    interface OnChoiceFragmentFinish {
        fun closeChoiceFragment(fragment: ChoiceFragment?, tour: Tour?)
    }

    companion object {
        // ----------------------------------
        // LIFECYCLE
        // ----------------------------------
        fun newInstance(tours: Tours?): ChoiceFragment {
            val fragment = ChoiceFragment()
            val args = Bundle()
            args.putSerializable(Tour.KEY_TOURS, tours)
            fragment.arguments = args
            return fragment
        }
    }
}