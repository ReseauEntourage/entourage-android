package social.entourage.android.deeplinks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import social.entourage.android.BuildConfig

class DeepLinksManagerTest {

    @Test
    fun testAllCatalogDeeplinksParsing() {
        DeeplinkTestData.getTestCases().forEach { testCase ->
            val resolvedView = DeepLinksManager.resolveDeepLinkView(testCase.uriString)
            assertNotNull("Failed to resolve view for URI: ${testCase.uriString}", resolvedView)
            assertEquals(
                "Mismatch for ${testCase.description} (${testCase.uriString})",
                testCase.expectedView,
                resolvedView
            )
        }
    }

    @Test
    fun testFindFirstDeeplinkInText_withCustomScheme() {
        val scheme = BuildConfig.DEEP_LINKS_SCHEME
        val text = "Bonjour, voici un lien vers l'action: $scheme://actions/1234 à consulter."

        val deeplink = DeepLinksManager.findFirstDeeplinkInText(text)
        assertNotNull(deeplink)
        assertEquals("$scheme://actions/1234", deeplink)
    }

    @Test
    fun testFindFirstDeeplinkInText_withHttpWebUrl() {
        val domain = BuildConfig.DEEP_LINKS_URL
        val text = "Rejoins-nous sur http://$domain/deeplink/action/5678 pour participer!"

        val deeplink = DeepLinksManager.findFirstDeeplinkInText(text)
        assertNotNull(deeplink)
        assertEquals("http://$domain/deeplink/action/5678", deeplink)
    }

    @Test
    fun testFindFirstDeeplinkInText_withoutDeeplink_returnsNull() {
        val text = "Un message simple sans aucun lien profond."
        val deeplink = DeepLinksManager.findFirstDeeplinkInText(text)
        assertNull(deeplink)
    }

    @Test
    fun testDeepLinksView_enumMappings() {
        assertEquals("deeplink", DeepLinksManager.DeepLinksView.DEEPLINK.view)
        assertEquals("badge", DeepLinksManager.DeepLinksView.BADGE.view)
        assertEquals("webview", DeepLinksManager.DeepLinksView.WEBVIEW.view)
        assertEquals("profile", DeepLinksManager.DeepLinksView.PROFILE.view)
        assertEquals("events", DeepLinksManager.DeepLinksView.EVENTS.view)
        assertEquals("actions", DeepLinksManager.DeepLinksView.APPLINK_ACTION.view)
    }
}
