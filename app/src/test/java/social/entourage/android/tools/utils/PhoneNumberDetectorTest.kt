package social.entourage.android.tools.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberDetectorTest {

    @Test
    fun `detecte les formats francais courants`() {
        listOf(
            "06 12 34 56 78",
            "0612345678",
            "06.12.34.56.78",
            "06-12-34-56-78",
            "+33 6 12 34 56 78",
            "+33612345678",
            "0033 6 12 34 56 78",
            "0033612345678",
            "+33 (0)6 12 34 56 78",
        ).forEach {
            assertTrue("devrait detecter : $it", PhoneNumberDetector.containsFrenchPhoneNumber(it))
        }
    }

    @Test
    fun `detecte un numero au milieu d'une phrase ou dans du HTML`() {
        assertTrue(PhoneNumberDetector.containsFrenchPhoneNumber("Appelle-moi au 06 12 34 56 78, merci !"))
        assertTrue(PhoneNumberDetector.containsFrenchPhoneNumber("<p>Mon numero : <b>0612345678</b></p>"))
    }

    @Test
    fun `ignore les textes sans numero de telephone`() {
        listOf(
            "",
            "Bonjour, comment allez-vous ?",
            "Rendez-vous le 12 03 2026 a 14h",
            "J'ai 2500 euros",
            "061234567",
            "06123456789",
            "00 00 00 00 00",
        ).forEach {
            assertFalse("ne devrait pas detecter : $it", PhoneNumberDetector.containsFrenchPhoneNumber(it))
        }
        assertFalse(PhoneNumberDetector.containsFrenchPhoneNumber(null))
    }
}
