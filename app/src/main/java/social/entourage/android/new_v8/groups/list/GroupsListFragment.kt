package social.entourage.android.new_v8.groups.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.databinding.NewFragmentGroupsListBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.models.Group


const val groupPerPage = 10

class GroupsListFragment : Fragment() {

    private var _binding: NewFragmentGroupsListBinding? = null
    val binding: NewFragmentGroupsListBinding get() = _binding!!
    private var groupsList: MutableList<Group> = ArrayList()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var page: Int = 0
    private var collapsed = false
    private var animation = 250L


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadGroups()
        groupPresenter.getAllGroups.observe(viewLifecycleOwner, ::handleResponseGetGroups)
        initializeGroups()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentGroupsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun handleResponseGetGroups(allGroups: MutableList<Group>?) {
        //groupsList.clear()
        allGroups?.let { groupsList.addAll(it) }
        binding.progressBar.visibility = View.GONE
        allGroups?.isEmpty()?.let { updateView(it) }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }


    private fun updateView(isListEmpty: Boolean) {
        if (isListEmpty) binding.emptyStateLayout.visibility = View.VISIBLE
        else binding.recyclerView.visibility = View.VISIBLE
    }


    private fun initializeGroups() {
        binding.recyclerView.apply {
            // Pagination
            addOnScrollListener(recyclerViewOnScrollListener)
            layoutManager = LinearLayoutManager(context)
            adapter = GroupsListAdapter(groupsList, object : OnItemCheckListener {
                override fun onItemCheck(item: Group) {
                }

                override fun onItemUncheck(item: Group) {
                }
            })
        }
    }

    private fun loadGroups() {
        page += 1
        groupPresenter.getAllGroups(page, groupPerPage)
    }

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                handleSearchBarVisibility(recyclerView)
                handlePagination(recyclerView)
            }
        }

    fun handleSearchBarVisibility(recyclerView: RecyclerView) {
        val offset = recyclerView.computeVerticalScrollOffset()
        val extent = recyclerView.computeVerticalScrollExtent()
        val range = recyclerView.computeVerticalScrollRange()
        val percentage = 100.0f * offset / (range - extent).toFloat()
        if (percentage < 1F) {
            if (collapsed) expand(binding.search)
        } else if (percentage > 5F) {
            if (!collapsed) collapse(binding.search)
        }
    }

    fun handlePagination(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
        layoutManager?.let {
            val visibleItemCount: Int = layoutManager.childCount
            val totalItemCount: Int = layoutManager.itemCount
            val firstVisibleItemPosition: Int =
                layoutManager.findFirstVisibleItemPosition()
            if (!groupPresenter.isLoading && !groupPresenter.isLastPage) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= groupPerPage) {
                    loadGroups()
                }
            }
        }
    }

    private fun expand(v: View) {
        val matchParentMeasureSpec =
            View.MeasureSpec.makeMeasureSpec((v.parent as View).width, View.MeasureSpec.EXACTLY)
        val wrapContentMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = v.measuredHeight

        v.layoutParams.height = 1
        v.visibility = View.VISIBLE
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                v.layoutParams.height =
                    if (interpolatedTime == 2f) LinearLayout.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        a.duration = animation
        v.startAnimation(a)
        collapsed = false
    }

    private fun collapse(v: View) {
        val initialHeight = v.measuredHeight
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 2f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height =
                        initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        a.duration = animation
        v.startAnimation(a)
        collapsed = true
    }
}