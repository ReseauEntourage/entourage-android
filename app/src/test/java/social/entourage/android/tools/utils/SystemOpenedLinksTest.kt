package social.entourage.android.tools.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemOpenedLinksTest {

    // ----- Liens Meet : ouverts par le système -----

    @Test
    fun meetLink_withScheme_isSystemOpened() {
        assertTrue(SystemOpenedLinks.shouldOpenWithSystem("https://meet.google.com/rzs-igyt-zpe"))
    }

    @Test
    fun meetLink_withMeetPathAndAuthuser_isSystemOpened() {
        assertTrue(SystemOpenedLinks.shouldOpenWithSystem("https://meet.google.com/meet/rzs-igyt-zpe?authuser=0"))
    }

    @Test
    fun meetLink_withoutScheme_isSystemOpened() {
        assertTrue(SystemOpenedLinks.shouldOpenWithSystem("meet.google.com/rzs-igyt-zpe"))
    }

    @Test
    fun meetLink_http_isSystemOpened() {
        assertTrue(SystemOpenedLinks.shouldOpenWithSystem("http://meet.google.com/rzs-igyt-zpe"))
    }

    @Test
    fun meetLink_uppercase_isSystemOpened() {
        assertTrue(SystemOpenedLinks.shouldOpenWithSystem("HTTPS://MEET.GOOGLE.COM/rzs-igyt-zpe"))
    }

    @Test
    fun meetLink_withSurroundingSpaces_isSystemOpened() {
        assertTrue(SystemOpenedLinks.shouldOpenWithSystem("  https://meet.google.com/rzs-igyt-zpe\n"))
    }

    @Test
    fun meetLink_withPort_isSystemOpened() {
        assertTrue(SystemOpenedLinks.shouldOpenWithSystem("https://meet.google.com:443/rzs-igyt-zpe"))
    }

    // ----- Hôtes qui ressemblent à Meet : jamais ouverts par le système -----

    @Test
    fun hostWithMeetAsPrefixOfAnotherDomain_isNotSystemOpened() {
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("https://meet.google.com.evil.tld/rzs-igyt-zpe"))
    }

    @Test
    fun hostEndingWithMeetHost_isNotSystemOpened() {
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("https://notmeet.google.com/rzs-igyt-zpe"))
    }

    @Test
    fun otherGoogleSubdomain_isNotSystemOpened() {
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("https://www.google.com/rzs-igyt-zpe"))
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("https://docs.google.com/document/d/1"))
    }

    @Test
    fun meetHostOnlyInUserInfo_isNotSystemOpened() {
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("https://meet.google.com@evil.tld/rzs-igyt-zpe"))
    }

    @Test
    fun meetHostOnlyInPathOrQuery_isNotSystemOpened() {
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("https://evil.tld/meet.google.com/rzs-igyt-zpe"))
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("https://evil.tld/?u=https://meet.google.com/rzs-igyt-zpe"))
    }

    @Test
    fun nonHttpScheme_isNotSystemOpened() {
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("javascript://meet.google.com/%0Aalert(1)"))
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("ftp://meet.google.com/rzs-igyt-zpe"))
    }

    // ----- Autres liens et entrées invalides -----

    @Test
    fun entourageAndOtherWebLinks_areNotSystemOpened() {
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("https://www.entourage.social/app/actions/123"))
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("https://example.org/page"))
    }

    @Test
    fun nullEmptyOrInvalidUrl_isNotSystemOpened() {
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem(null))
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem(""))
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("   "))
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("https://meet.google.com/a b c"))
        assertFalse(SystemOpenedLinks.shouldOpenWithSystem("not a url"))
    }
}
