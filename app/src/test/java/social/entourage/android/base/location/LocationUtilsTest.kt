package social.entourage.android.base.location

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.location.Priority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import social.entourage.android.base.location.LocationUtils.getFromLocationCompat
import social.entourage.android.base.location.LocationUtils.getFromLocationNameCompat
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class LocationUtilsTest {

    @Test
    fun testCreateLocationRequestConfig() {
        val request = LocationUtils.createLocationRequest()

        assertNotNull(request)
        assertEquals(Priority.PRIORITY_HIGH_ACCURACY, request.priority)
        assertEquals(TimeUnit.SECONDS.toMillis(LocationUtils.DURATION_INTERVAL_PUBLIC), request.intervalMillis)
        assertEquals(TimeUnit.SECONDS.toMillis(LocationUtils.DURATION_FAST_INTERVAL_PUBLIC), request.minUpdateIntervalMillis)
    }

    @Test
    fun testGetFromLocationCompatFallback() {
        val geocoder = mock(Geocoder::class.java)
        val mockAddress = mock(Address::class.java)
        val mockAddresses = listOf(mockAddress)

        `when`(geocoder.getFromLocation(anyDouble(), anyDouble(), anyInt())).thenReturn(mockAddresses)

        var resultAddresses: List<Address>? = null
        geocoder.getFromLocationCompat(48.8566, 2.3522, 1) { addresses ->
            resultAddresses = addresses
        }

        assertNotNull(resultAddresses)
        assertEquals(1, resultAddresses?.size)
        assertEquals(mockAddress, resultAddresses?.get(0))
    }

    @Test
    fun testGetFromLocationNameCompatFallback() {
        val geocoder = mock(Geocoder::class.java)
        val mockAddress = mock(Address::class.java)
        val mockAddresses = listOf(mockAddress)

        `when`(geocoder.getFromLocationName(anyString(), anyInt())).thenReturn(mockAddresses)

        var resultAddresses: List<Address>? = null
        geocoder.getFromLocationNameCompat("Paris", 1) { addresses ->
            resultAddresses = addresses
        }

        assertNotNull(resultAddresses)
        assertEquals(1, resultAddresses?.size)
        assertEquals(mockAddress, resultAddresses?.get(0))
    }
}
