package social.entourage.android.tools.utils

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.Serializable

@Suppress("DEPRECATION")
class CompatExtensionsTest {

    data class TestSerializable(val id: Int, val name: String) : Serializable

    class TestParcelable(val id: Int, val title: String) : Parcelable {
        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) {}
    }

    @Test
    fun testIntentSerializableExtra() {
        val intent = mock(Intent::class.java)
        val original = TestSerializable(42, "Entourage")
        `when`(intent.getSerializableExtra(eq("key_serializable"))).thenReturn(original)
        `when`(intent.getSerializableExtra(eq("key_serializable"), any<Class<TestSerializable>>())).thenReturn(original)

        val retrieved: TestSerializable? = intent.serializableExtra("key_serializable")
        assertNotNull(retrieved)
        assertEquals(42, retrieved?.id)
        assertEquals("Entourage", retrieved?.name)
    }

    @Test
    fun testBundleSerializableCompat() {
        val bundle = mock(Bundle::class.java)
        val original = TestSerializable(99, "TestBundle")
        `when`(bundle.getSerializable(eq("key_bundle_serializable"))).thenReturn(original)
        `when`(bundle.getSerializable(eq("key_bundle_serializable"), any<Class<TestSerializable>>())).thenReturn(original)

        val retrieved: TestSerializable? = bundle.serializableCompat("key_bundle_serializable")
        assertNotNull(retrieved)
        assertEquals(99, retrieved?.id)
        assertEquals("TestBundle", retrieved?.name)
    }

    @Test
    fun testIntentParcelableExtra() {
        val intent = mock(Intent::class.java)
        val original = TestParcelable(101, "ParcelableIntent")
        `when`(intent.getParcelableExtra<TestParcelable>(eq("key_parcelable"))).thenReturn(original)
        `when`(intent.getParcelableExtra(eq("key_parcelable"), any<Class<TestParcelable>>())).thenReturn(original)

        val retrieved: TestParcelable? = intent.parcelableExtra("key_parcelable")
        assertNotNull(retrieved)
        assertEquals(101, retrieved?.id)
        assertEquals("ParcelableIntent", retrieved?.title)
    }

    @Test
    fun testBundleParcelableCompat() {
        val bundle = mock(Bundle::class.java)
        val original = TestParcelable(202, "ParcelableBundle")
        `when`(bundle.getParcelable<TestParcelable>(eq("key_bundle_parcelable"))).thenReturn(original)
        `when`(bundle.getParcelable(eq("key_bundle_parcelable"), any<Class<TestParcelable>>())).thenReturn(original)

        val retrieved: TestParcelable? = bundle.parcelableCompat("key_bundle_parcelable")
        assertNotNull(retrieved)
        assertEquals(202, retrieved?.id)
        assertEquals("ParcelableBundle", retrieved?.title)
    }

    @Test
    fun testMissingExtraReturnsNull() {
        val intent = mock(Intent::class.java)
        val bundle = mock(Bundle::class.java)

        assertNull(intent.serializableExtra<TestSerializable>("unknown_key"))
        assertNull(bundle.serializableCompat<TestSerializable>("unknown_key"))
        assertNull(intent.parcelableExtra<TestParcelable>("unknown_key"))
        assertNull(bundle.parcelableCompat<TestParcelable>("unknown_key"))
    }
}
