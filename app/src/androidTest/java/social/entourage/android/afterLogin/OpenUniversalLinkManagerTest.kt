package social.entourage.android.afterLogin

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.junit.Before
import org.junit.Rule
import social.entourage.android.EntourageApplication

@LargeTest
//@RunWith(AndroidJUnit4::class)
abstract class OpenUniversalLinkManagerTest : EntourageTestAfterLogin() {

    val context: Context = ApplicationProvider.getApplicationContext<EntourageApplication>()

    // This rule will grant the POST_NOTIFICATIONS permission before each test in this class
    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @Before
    open fun setUp() {
        //context = ApplicationProvider.getApplicationContext<EntourageApplication>()
        super.setUp(context)
    }

    companion object {
        const val URL = "https://preprod.entourage.social/app/"

        enum class EntourageLink(val link: String) {
            HOME(URL),
            GROUP(URL +"groups/bb8c3e77aa95"),
            GROUP_LIST(URL +"groups"),
            OUTING(URL +"outings/ebJUCN-woYgM"),
            OUTINGS_LIST(URL +"outings"),
            NEW_CONTRIBUTION(URL +"contributions/new"),
            NEW_SOLICITATION(URL +"solicitations/new"),
            CONTRIBUTIONS_LIST(URL +"contributions"),
            SOLICITATIONS_LIST(URL +"solicitations"),
            CONTRIBUTION_DETAIL(URL +"contributions/er2BVAa5Vb4U"),
            SOLICITATION_DETAIL(URL +"solicitations/eibewY3GW-ek")
        }
    }
}