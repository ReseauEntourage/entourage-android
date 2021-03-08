package social.entourage.android.newsfeed.v2

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_announcements_feed.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.feed.Announcement
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.base.BackPressable
import social.entourage.android.newsfeed.*
import social.entourage.android.service.EntService
import social.entourage.android.tools.view.EntSnackbar
import timber.log.Timber


/**
 * Created by Jr on 3/1/21.
 */
class AnnouncementsFeedFragment : Fragment(), BackPressable, NewsFeedListener, AnnouncementViewHolderListener {

    var _array = ArrayList<Announcement>()
    protected var entService: EntService? = null
    var pagination = NewsfeedPagination()
    val selectedTab = NewsfeedTabItem.ANNOUNCEMENTS
    private val connection = ServiceConnection()

    var adapter:AnnouncementAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       connection.doBindService()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_announcements_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui_rv_announcements?.layoutManager = LinearLayoutManager(context)

        adapter = AnnouncementAdapter()
        adapter?.viewHolderListener = this
        ui_rv_announcements?.adapter = adapter

        pagination = NewsfeedPagination()
        pagination.page = 0
        pagination.isLoading = false

        entService?.updateNewsfeed(pagination, selectedTab)

        ui_bt_back?.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onBackPressed(): Boolean {
        requireActivity().supportFragmentManager.popBackStack(BaseNewsfeedFragment.TAG,0)

        val editor = EntourageApplication.get().sharedPreferences.edit()
        editor.putBoolean("isNavNews",false)
        editor.putString("navType",null)
        editor.apply()

        return true
    }

    override fun onDestroy() {
        connection.doUnbindService()
        super.onDestroy()
    }

    /*****
     * NewsFeedListener
     */

    override fun onNewsFeedReceived(newsFeeds: List<NewsfeedItem>) {
         _array = ArrayList()
        for (newsfeed in newsFeeds) {
            (newsfeed.data as? Announcement)?.let {
                _array.add(it)
            }
        }
        adapter?.updateDatas(_array)

        pagination.isLoading = false
        pagination.isRefreshing = false
    }

    /*****
     * Listener click detail announce
     */
    override fun onDetailClicked(position: Int) {
        val _url = _array[position]
        Timber.d("***** ici Click detail View pos $position --- ${_url.url}")

        if (_url.url == null) return
        val actIntent = Intent(Intent.ACTION_VIEW, Uri.parse(_url.url))
        try {
            requireActivity().startActivity(actIntent)
        } catch (ex: Exception) {
            view?.let { EntSnackbar.make(it, R.string.no_browser_error, Snackbar.LENGTH_SHORT).show() }
        }
    }

    /*****
     * ApiConnectionListeners
     */
    override fun onNetworkException() {
        ui_main_announcement?.let {EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()}
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

    override fun onServerException(throwable: Throwable) {
        ui_main_announcement?.let {EntSnackbar.make(it, R.string.server_error, Snackbar.LENGTH_LONG).show()}
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

    override fun onTechnicalException(throwable: Throwable) {
        ui_main_announcement?.let { EntSnackbar.make(it, R.string.technical_error, Snackbar.LENGTH_LONG).show() }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

    /******
     * ServiceConnection Class
     */
    private inner class ServiceConnection : android.content.ServiceConnection {
        private var isBound = false

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity == null) {
                isBound = false
                Timber.e("No activity for service")
                return
            }
            entService = (service as EntService.LocalBinder).service
            entService?.let {
              //  it.registerServiceListener(this@AnnouncementsFeedFragment)
                it.registerApiListener(this@AnnouncementsFeedFragment)
                it.updateNewsfeed(pagination, selectedTab)
                isBound = true
            } ?: run {
                Timber.e("Service not found")
                isBound = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
           // entService?.unregisterServiceListener(this@AnnouncementsFeedFragment)
            entService?.unregisterApiListener(this@AnnouncementsFeedFragment)
            entService = null
            isBound = false
        }
        // ----------------------------------
        // SERVICE BINDING METHODS
        // ----------------------------------
        fun doBindService() {
            if(isBound) return
            activity?.let {
                if(EntourageApplication.me(it) ==null) {
                    // Don't start the service
                    return
                }
                try {
                    val intent = Intent(it, EntService::class.java)
                    it.startService(intent)
                    it.bindService(intent, this, Context.BIND_AUTO_CREATE)
                } catch (e: IllegalStateException) {
                    Timber.w(e)
                }
            }
        }

        fun doUnbindService() {
            if (!isBound) return
            activity?.unbindService(this)
            isBound = false
        }
    }
}