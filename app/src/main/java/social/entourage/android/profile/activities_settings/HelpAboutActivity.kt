package social.entourage.android.profile.activities_settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.material.snackbar.Snackbar
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.profile.oss.OSSLibsActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.ui.theme.EntourageComposeStyles

class HelpAboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            HelpAboutScreen(
                onBack = { finish() },
                onTermsClicked = { onTermsClicked() },
                onPrivacyClicked = { onPrivacyClicked() },
                onRateUsClicked = { onRateUsClicked() },
                onFAQClicked = { onFAQClicked() },
                onDonate = { onDonate() },
                onAmbassadorClicked = { onAmbassadorClicked() },
                onEthicChartClicked = { onEthicChartClicked() },
                onPartnerClicked = { onPartnerClicked() },
                onChildRuleClicked = { onChildRuleClicked() },
                onOSSLicensesClicked = { onOSSLicensesClicked() }
            )
        }
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    private fun onTermsClicked() {
        openLink(getString(R.string.terms_url))
    }

    private fun onPrivacyClicked() {
        val privacyUrl = getString(R.string.privacy_policy_url)
        openLink(privacyUrl)
    }

    private fun onChildRuleClicked() {
        val privacyUrl = getString(R.string.child_rule_url)
        openLink(privacyUrl)
    }

    private fun onOSSLicensesClicked() {
        startActivity(Intent(this, OSSLibsActivity::class.java))
    }

    private fun onRateUsClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_RATING)
        val goToMarket = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(getString(R.string.market_url, packageName))
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            openLink(getString(R.string.playstore_url, packageName))
        }
    }

    private fun onDonate() {
        // À l'origine, on récupérait un lien spécifique via ProfileActivity.
        // On utilise ici un lien par défaut défini dans les ressources.
        val link = getString(R.string.donate_default_link)
        openLink(link)
    }

    private fun onFAQClicked() {
        val FAQUrl = getString(R.string.faq_link_public)
        openLink(FAQUrl)
    }

    private fun onAmbassadorClicked() {
        val ambassadorUrl = getString(R.string.ambassadeur_link_public)
        openLink(ambassadorUrl)
    }

    private fun onEthicChartClicked() {
        val chartUrl = getString(R.string.disclaimer_link_public)
        openLink(chartUrl)
    }

    private fun onPartnerClicked() {
        val partnerUrl = getString(R.string.url_app_partner)
        openLink(partnerUrl)
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (ex: Exception) {
            showNoBrowserError()
        }
    }

    private fun showNoBrowserError() {
        EntSnackbar.make(
            window.decorView.rootView,
            R.string.no_browser_error,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, HelpAboutActivity::class.java)
            context.startActivity(intent)
        }
    }
}

@Composable
fun HelpAboutScreen(
    onBack: () -> Unit,
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit,
    onRateUsClicked: () -> Unit,
    onFAQClicked: () -> Unit,
    onDonate: () -> Unit,
    onAmbassadorClicked: () -> Unit,
    onEthicChartClicked: () -> Unit,
    onPartnerClicked: () -> Unit,
    onChildRuleClicked: () -> Unit,
    onOSSLicensesClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(61.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(25.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_round),
                        contentDescription = stringResource(id = R.string.back),
                        tint = Color.Unspecified
                    )
                }

                Text(
                    text = stringResource(id = R.string.help_about),
                    style = EntourageComposeStyles.h2,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = colorResource(id = R.color.light_orange_opacity_50)
            )
        }

        // List
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimensionResource(id = R.dimen.padding_horizontal))
        ) {
            AboutItem(label = stringResource(id = R.string.ethics), onClick = onEthicChartClicked)
            AboutItem(label = stringResource(id = R.string.about_partner), onClick = onPartnerClicked)
            AboutItem(label = stringResource(id = R.string.ambassador_program), onClick = onAmbassadorClicked)
            AboutItem(label = stringResource(id = R.string.donation), onClick = onDonate)
            AboutItem(label = stringResource(id = R.string.faq), onClick = onFAQClicked)
            AboutItem(label = stringResource(id = R.string.feedback), onClick = onRateUsClicked)
            AboutItem(label = stringResource(id = R.string.cgu), onClick = onTermsClicked)
            AboutItem(label = stringResource(id = R.string.confidentiality), onClick = onPrivacyClicked)
            AboutItem(label = stringResource(id = R.string.child_rules), onClick = onChildRuleClicked)
            AboutItem(label = stringResource(id = R.string.about_oss_licenses), onClick = onOSSLicensesClicked)
        }
    }
}

@Composable
fun AboutItem(label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(top = 20.dp)
    ) {
        Text(
            text = label,
            style = EntourageComposeStyles.h4,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = Dp.Hairline,
            color = colorResource(id = R.color.light_orange)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HelpAboutScreenPreview() {
    HelpAboutScreen(
        onBack = {},
        onTermsClicked = {},
        onPrivacyClicked = {},
        onRateUsClicked = {},
        onFAQClicked = {},
        onDonate = {},
        onAmbassadorClicked = {},
        onEthicChartClicked = {},
        onPartnerClicked = {},
        onChildRuleClicked = {},
        onOSSLicensesClicked = {}
    )
}
