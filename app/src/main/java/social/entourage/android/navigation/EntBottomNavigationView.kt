package social.entourage.android.navigation

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import me.leolin.shortcutbadger.ShortcutBadger
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.configuration.Configuration
import social.entourage.android.newsfeed.BaseNewsfeedFragment
import social.entourage.android.newsfeed.v2.NewHomeFeedFragment
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import kotlin.math.roundToInt

class EntBottomNavigationView : BottomNavigationView {
    private val navigationDataSource: BottomNavigationDataSource = BottomNavigationDataSource()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context)  : super(context)

    fun configure(activity: MainActivity) {
        instance = this
        // we need to set the listener first, to respond to the default selected tab request
        this.setOnItemSelectedListener { item: MenuItem ->
            if (shouldBypassNavigation(activity, item.itemId)) {
                return@setOnItemSelectedListener false
            }
            if (this.selectedItemId != item.itemId) {
                sendAnalyticsTapTabbar(item.itemId)
                loadFragment(activity.supportFragmentManager, item.itemId)
            }
            true
        }
        val defaultId = navigationDataSource.defaultSelectedTab
        this.selectedItemId = defaultId
        loadFragment(activity.supportFragmentManager, defaultId)
        val messageBadge = this.getOrCreateBadge(navigationDataSource.myMessagesTabIndex)
        messageBadge.backgroundColor = ResourcesCompat.getColor(resources, R.color.map_announcement_background, null)
        messageBadge.badgeTextColor = ResourcesCompat.getColor(resources, R.color.primary, null)
        messageBadge.maxCharacterCount = 3

        configurePlusButton()
    }

    private fun configurePlusButton() {
        this.findViewById<BottomNavigationItemView>(navigationDataSource.actionMenuId)
            ?.findViewById<ImageView>(com.google.android.material.R.id.navigation_bar_item_icon_view)?.let { plusIcon ->
                scaleView(plusIcon, 1.5f)
                plusIcon.setColorFilter(ContextCompat.getColor(context, R.color.accent))
            }
    }

    fun updatePlusButton(badge: Boolean) {
        this.findViewById<BottomNavigationItemView>(navigationDataSource.actionMenuId)
            ?.findViewById<ImageView>(com.google.android.material.R.id.navigation_bar_item_icon_view)
            ?.setColorFilter(ContextCompat.getColor(context, if(badge) R.color.dodger_blue else R.color.accent))
    }

    private fun scaleView(view: View, scale: Float) {
        val layoutParams = view.layoutParams as LayoutParams
        layoutParams.width = (scale * layoutParams.width).roundToInt()
        layoutParams.height = (scale * layoutParams.height).roundToInt()
        view.layoutParams = layoutParams
    }

    private fun shouldBypassNavigation(activity: MainActivity, @IdRes itemId: Int): Boolean {
        if (itemId == navigationDataSource.actionMenuId) {
            //Handling special cases
            if (!Configuration.showPlusScreen()) {
                // Show directly the create entourage disclaimer
                activity.createEntourage()
                return true
            } else if (activity.authenticationController.savedTour != null) {
                // Show directly the create encounter
                //TODO should be bound to service
                activity.addEncounter()
                return true
            }
        }
        return false
    }


    private fun loadFragment(supportFragmentManager: FragmentManager, menuId: Int) {
        try {
            val tag = navigationDataSource.getFragmentTagAtIndex(menuId)
            if (!supportFragmentManager.popBackStackImmediate(tag, 0)) {
                val newFragment = navigationDataSource.getFragmentAtIndex(menuId) ?: return
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.main_fragment, newFragment, tag)
                fragmentTransaction.addToBackStack(tag)
                fragmentTransaction.commit()
            }
            else {
                if (tag == BaseNewsfeedFragment.TAG) {
                    if (supportFragmentManager.fragments.first() != null && (supportFragmentManager.fragments.first() is NewHomeFeedFragment) ) {
                        (supportFragmentManager.fragments.first() as NewHomeFeedFragment).checkNavigation()
                    }
                }
            }
            //TODO check if we need to execute pending actions
            //supportFragmentManager.executePendingTransactions();
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }
    private fun selectNavigationTab(menuIndex: Int) {
        //TODO: Voir pour afficher le bon fragment de l'onglet Home si on est en nav horizontale
        if (selectedItemId != menuIndex) {
            selectedItemId = menuIndex
        }
    }

    fun showFeed() {
        selectNavigationTab(navigationDataSource.feedTabIndex)
    }

    fun showGuide() {
        selectNavigationTab(navigationDataSource.guideTabIndex)
    }

    fun showEvents() {
        selectNavigationTab(navigationDataSource.feedTabIndex)
    }

    fun showAllActions() {
        selectNavigationTab(navigationDataSource.feedTabIndex)
    }

    fun showMyEntourages() {
        selectNavigationTab(navigationDataSource.myMessagesTabIndex)
    }

    fun showActionsTab() {
        selectNavigationTab(navigationDataSource.actionMenuId)
    }

    fun showProfileTab() {
        selectNavigationTab(navigationDataSource.profilTabIndex)
    }

    private fun sendAnalyticsTapTabbar(@IdRes itemId: Int) {
        when (itemId) {
            R.id.bottom_bar_newsfeed -> AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_TAB_FEEDS)
            R.id.bottom_bar_guide -> AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_TAB_GDS)
            R.id.bottom_bar_plus -> AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_TAB_PLUS)
            R.id.bottom_bar_mymessages -> AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_TAB_MESSAGES)
            R.id.bottom_bar_profile -> AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_TAB_PROFIL)
        }
    }

    // ----------------------------------
    // Helper functions
    // ----------------------------------
    fun refreshBadgeCount() {
        try {
            val messageBadge = getOrCreateBadge(navigationDataSource.myMessagesTabIndex)
            if (badgeCount > 0) {
                messageBadge.isVisible = true
                messageBadge.number = badgeCount
                ShortcutBadger.applyCount(EntourageApplication.get().applicationContext, badgeCount)
            } else {
                messageBadge.isVisible = false
                ShortcutBadger.removeCount(EntourageApplication.get().applicationContext)
            }
        } catch (e: Exception) {
            Timber.w(e)
        }
    }

    fun updateBadgeCountForUser(count: Int) {
        badgeCount = count
        refreshBadgeCount()
    }

    companion object {
        private var instance:EntBottomNavigationView?  = null
        private var badgeCount = 0

        fun increaseBadgeCount() {
            badgeCount++
            instance?.refreshBadgeCount()
        }

        fun decreaseBadgeCount() {
            badgeCount--
            instance?.refreshBadgeCount()
        }

        fun resetBadgeCount() {
            badgeCount = 0
            instance?.refreshBadgeCount()
        }

        fun updatePlusBadge(show: Boolean) {
            instance?.updatePlusButton(show)
        }
    }

}