package social.entourage.android.deeplinks

import social.entourage.android.BuildConfig

/**
 * Centralized catalog of test cases for Deeplinks.
 * Used both by JVM unit tests and Instrumented Espresso UI tests.
 */
data class DeeplinkTestCase(
    val description: String,
    val uriString: String,
    val expectedView: DeepLinksManager.DeepLinksView,
    val targetViewIdName: String? = null
)

object DeeplinkTestData {
    fun getTestCases(): List<DeeplinkTestCase> {
        val scheme = BuildConfig.DEEP_LINKS_SCHEME
        val domain = BuildConfig.DEEP_LINKS_URL

        return listOf(
            // Custom schemes
            DeeplinkTestCase(
                description = "Custom scheme - Guide",
                uriString = "$scheme://guide",
                expectedView = DeepLinksManager.DeepLinksView.GUIDE,
                targetViewIdName = "fragment_guide"
            ),
            DeeplinkTestCase(
                description = "Custom scheme - Events",
                uriString = "$scheme://events",
                expectedView = DeepLinksManager.DeepLinksView.EVENTS,
                targetViewIdName = "fragment_events"
            ),
            DeeplinkTestCase(
                description = "Custom scheme - Profile",
                uriString = "$scheme://profile",
                expectedView = DeepLinksManager.DeepLinksView.PROFILE,
                targetViewIdName = "fragment_profile"
            ),
            DeeplinkTestCase(
                description = "Custom scheme - Badge",
                uriString = "$scheme://badge",
                expectedView = DeepLinksManager.DeepLinksView.BADGE
            ),
            DeeplinkTestCase(
                description = "Custom scheme - Create Action",
                uriString = "$scheme://create-action",
                expectedView = DeepLinksManager.DeepLinksView.CREATE_ACTION
            ),
            DeeplinkTestCase(
                description = "Custom scheme - Tutorial",
                uriString = "$scheme://tutorial",
                expectedView = DeepLinksManager.DeepLinksView.TUTORIAL
            ),
            DeeplinkTestCase(
                description = "Custom scheme - Guide Map",
                uriString = "$scheme://guidemap",
                expectedView = DeepLinksManager.DeepLinksView.GUIDE_MAP
            ),
            DeeplinkTestCase(
                description = "Custom scheme - Webview with URL",
                uriString = "$scheme://webview?url=https://entourage.social",
                expectedView = DeepLinksManager.DeepLinksView.WEBVIEW
            ),

            // Official Web Links (/deeplink/ & /app/)
            DeeplinkTestCase(
                description = "HTTP web link - Deeplink Guide",
                uriString = "http://$domain/deeplink/guide",
                expectedView = DeepLinksManager.DeepLinksView.GUIDE
            ),
            DeeplinkTestCase(
                description = "HTTPS web link - Deeplink Profile",
                uriString = "https://$domain/deeplink/profile",
                expectedView = DeepLinksManager.DeepLinksView.PROFILE
            ),
            DeeplinkTestCase(
                description = "HTTPS web link - Deeplink Events",
                uriString = "https://$domain/deeplink/events",
                expectedView = DeepLinksManager.DeepLinksView.EVENTS
            ),
            DeeplinkTestCase(
                description = "HTTPS web link - Deeplink Create Action",
                uriString = "https://$domain/deeplink/create-action",
                expectedView = DeepLinksManager.DeepLinksView.CREATE_ACTION
            ),
            DeeplinkTestCase(
                description = "HTTPS App link - Homepage",
                uriString = "https://$domain/app/",
                expectedView = DeepLinksManager.DeepLinksView.GUIDE
            ),
            DeeplinkTestCase(
                description = "HTTPS App link - Groups",
                uriString = "https://$domain/app/groups",
                expectedView = DeepLinksManager.DeepLinksView.ENTOURAGES
            ),
            DeeplinkTestCase(
                description = "HTTPS App link - Group Detail",
                uriString = "https://$domain/app/neighborhoods/b207272a5541",
                expectedView = DeepLinksManager.DeepLinksView.ENTOURAGE
            ),
            DeeplinkTestCase(
                description = "HTTPS App link - Outings",
                uriString = "https://$domain/app/outings",
                expectedView = DeepLinksManager.DeepLinksView.EVENTS
            ),
            DeeplinkTestCase(
                description = "HTTPS App link - Outings New",
                uriString = "https://$domain/app/outings/new",
                expectedView = DeepLinksManager.DeepLinksView.EVENTS
            ),
            DeeplinkTestCase(
                description = "HTTPS App link - Contributions",
                uriString = "https://$domain/app/contributions",
                expectedView = DeepLinksManager.DeepLinksView.CREATE_ACTION
            ),
            DeeplinkTestCase(
                description = "HTTPS App link - Solicitations",
                uriString = "https://$domain/app/solicitations",
                expectedView = DeepLinksManager.DeepLinksView.CREATE_ACTION
            ),
            DeeplinkTestCase(
                description = "HTTPS App link - Map",
                uriString = "https://$domain/app/map",
                expectedView = DeepLinksManager.DeepLinksView.GUIDE_MAP
            ),
            DeeplinkTestCase(
                description = "HTTPS App link - Badges",
                uriString = "https://$domain/app/badges",
                expectedView = DeepLinksManager.DeepLinksView.BADGE
            ),
            DeeplinkTestCase(
                description = "HTTPS App link - Resources",
                uriString = "https://$domain/app/resources",
                expectedView = DeepLinksManager.DeepLinksView.GUIDE
            ),
            DeeplinkTestCase(
                description = "HTTPS App link - Resource Detail",
                uriString = "https://$domain/app/resources/aa820899a375",
                expectedView = DeepLinksManager.DeepLinksView.GUIDE
            )
        )
    }
}
