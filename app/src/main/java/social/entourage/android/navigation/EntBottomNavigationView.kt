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
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
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
        // we need to set the listener first, to respond to the default selected tab request
        this.setOnNavigationItemSelectedListener { item: MenuItem ->
            if (shouldBypassNavigation(activity, item.itemId)) {
                return@setOnNavigationItemSelectedListener false
            }
            if (this.selectedItemId != item.itemId) {
                sendAnalyticsTapTabbar(item.itemId)
                loadFragment(activity.supportFragmentManager, item.itemId)
            }
            true
        }
        //TODO: a remettre l'auto ?
        //navigationDataSource.isEngaged = authenticationController.me?.isEngaged ?: false
        navigationDataSource.isEngaged = true
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
        val plusIcon = getPlusIconIv()
        scaleView(plusIcon, 1.5f)
        plusIcon.setColorFilter(ContextCompat.getColor(context, R.color.accent))
    }

    private fun getPlusIconIv(): ImageView {
        val menuView = getChildAt(0) as BottomNavigationMenuView
        val plusItemId = menu.getItem(2).itemId
        val plusItem = menuView.findViewById<View>(plusItemId)
        return plusItem.findViewById(com.google.android.material.R.id.icon)
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


    fun loadFragment(supportFragmentManager: FragmentManager, menuId: Int) {
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
        //TODO: Voir pour afficher le bon frgagment de l'onglet Home si on est en nav horizontale
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

    fun sendAnalyticsTapTabbar(@IdRes itemId: Int) {
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
        val messageBadge = getOrCreateBadge(navigationDataSource.myMessagesTabIndex)
            ?: return
        val badgeCount = EntourageApplication.get().badgeCount
        if (badgeCount > 0) {
            messageBadge.isVisible = true
            messageBadge.number = badgeCount
        } else {
            messageBadge.isVisible = false
        }
    }

}