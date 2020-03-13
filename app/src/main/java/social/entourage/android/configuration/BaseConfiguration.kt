package social.entourage.android.configuration

/**
 * Configuration class that will be subclassed in each app
 * Created by Mihai Ionescu on 02/05/2018.
 */
abstract class BaseConfiguration {
    // In case of an lost code error, show the toast or a separate screen
    var showLostCodeErrorToast = true
    // Show the tutorial for the first time users
    var showTutorial = false
    // Show the user edit screen
    var showUserEditProfile = true
    // Show the edit entourage screen from 14.2 .If false, send an email
    var showEditEntourageView = true
    // Show the invite screen in 14.2 . If false, show the share screen
    var showInviteView = true
    // Show the Plus Screen Menu in screens 06.1 and 06.2. If false, it shows the entourage disclaimer directly
    var showPlusScreen = true
    // Show the entourage disclaimer screen. If false, show directly the create entourage screen
    var showEntourageDisclaimer = true

    fun showLostCodeErrorToast(): Boolean {
        return showLostCodeErrorToast
    }

    fun showTutorial(): Boolean {
        return showTutorial
    }

    fun showUserEditProfile(): Boolean {
        return showUserEditProfile
    }

    fun showEditEntourageView(): Boolean {
        return showEditEntourageView
    }

    fun showInviteView(): Boolean {
        return showInviteView
    }

    fun showPlusScreen(): Boolean {
        return showPlusScreen
    }

    fun showEntourageDisclaimer(): Boolean {
        return showEntourageDisclaimer
    }
}