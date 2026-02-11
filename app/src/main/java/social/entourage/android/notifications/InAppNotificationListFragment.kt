package social.entourage.android.notifications

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.api.model.notification.InAppNotification
import social.entourage.android.databinding.NewFragmentNotifsInAppListBinding
import social.entourage.android.home.HomePresenter
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import timber.log.Timber

class InAppNotificationListFragment : Fragment() {
    private val groupPerPage = 10
    private var _binding: NewFragmentNotifsInAppListBinding? = null
    val binding: NewFragmentNotifsInAppListBinding get() = _binding!!

    private val homePresenter: HomePresenter by lazy { HomePresenter() }

    var notifications = ArrayList<InAppNotification>()

    private var page: Int = 0
    private var itemSelected = -1

    private var hasToShowDot = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentNotifsInAppListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val countNotif = arguments?.getInt(Const.NOTIF_COUNT)
        hasToShowDot = (countNotif ?: 0) > 0

        handleBackButton()
        homePresenter.notificationsInApp.observe(
            viewLifecycleOwner,
            ::handleGetNotifications
        )
        homePresenter.notificationInApp.observe(
            viewLifecycleOwner,
            ::handleUpdateNotification
        )

        initializeRecyclerView()
        loadNotifs()

        handleSwipeRefresh()
        AnalyticsEvents.logEvent(AnalyticsEvents.Home_view_notif)
        checkUnread()
    }

    private fun handleUpdateNotification(notif: InAppNotification?) {
        if (itemSelected != -1 && itemSelected < notifications.size && notif != null) {
            notifications[itemSelected] = notif
            itemSelected = -1
            binding.recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun handleGetNotifications(notifs:MutableList<InAppNotification>?) {
        notifs?.let { notifications.addAll(it) }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun checkUnread() {
        if (hasToShowDot) {
            binding.iconBell.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_new_notif_on))
            val timer = object: CountDownTimer(2000, 1000) {
                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    if(context != null) {
                        binding.iconBell.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_new_notif_off))
                    }
                }
            }
            timer.start()
        }
        else {
            binding.iconBell.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_new_notif_off))
        }
    }

    private fun loadNotifs() {
        binding.swipeRefresh.isRefreshing = false
        page++
        homePresenter.getNotifications(page,groupPerPage)
    }

    private fun handleSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            notifications.clear()
            binding.recyclerView.adapter?.notifyDataSetChanged()
            homePresenter.notificationsInApp.value?.clear()
            homePresenter.isLastPage = false
            page = 0
            loadNotifs()
        }
    }

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                handlePagination(recyclerView)
            }
        }

    fun handlePagination(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
        layoutManager?.let {
            val visibleItemCount: Int = layoutManager.childCount
            val totalItemCount: Int = layoutManager.itemCount
            val firstVisibleItemPosition: Int =
                layoutManager.findFirstVisibleItemPosition()
            if (!homePresenter.isLoading && !homePresenter.isLastPage) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= groupPerPage) {
                    loadNotifs()
                }
            }
        }
    }

    private fun handleBackButton() {
        binding.iconBack.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun initializeRecyclerView() {
        binding.recyclerView.apply {
            // Pagination
            addOnScrollListener(recyclerViewOnScrollListener)
            layoutManager = LinearLayoutManager(context)
            adapter = InAppListNotificationsAdapter(notifications, object : OnItemClick {
                override fun onItemClick(notif: InAppNotification, position:Int) {

                    val instance = notif.instanceType
                    val instanceId = notif.instanceId ?: 0
                    val stage = notif.context
                    val notifContext = notif.context
                    val postId:Int? = notif.postId
                    if (notif.completedAt == null) {
                        itemSelected = position
                        notif.id?.let { homePresenter.markReadNotification(notif.id) }
                    }
                    else {
                        itemSelected = -1
                    }
                    if(instance != null) {
                        NotificationActionManager.presentAction(requireContext(),parentFragmentManager,instance,instanceId,postId,stage, notifContext = notifContext )
                    } else{
                        NotificationActionManager.presentWelcomeAction(requireContext(), stage)
                    }
                }
            })
        }
    }
}