package com.example.util

import android.content.Context
import android.location.Location
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LocationSecurityUtil {

    // Global mock GPS state (shared between Dashboard, Pengajuan, and other screens)
    private val _isMockGpsActive = MutableStateFlow(false)
    val isMockGpsActive: StateFlow<Boolean> = _isMockGpsActive.asStateFlow()

    fun setMockGpsActive(active: Boolean) {
        _isMockGpsActive.value = active
    }

    /**
     * Checks if a given Android Location is from a mock provider.
     */
    fun isLocationMock(location: Location?): Boolean {
        if (location == null) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
    }

    /**
     * Checks if mock location / fake GPS setting is enabled on device or simulator.
     */
    fun isMockSettingsEnabled(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ALLOW_MOCK_LOCATION
                ) != "0"
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Returns standard warning message for mock GPS detection.
     */
    const val MOCK_GPS_WARNING_MESSAGE =
        "Terdeteksi menggunakan Fake GPS / Mock GPS! Fitur presensi (masuk/pulang) dan pengajuan izin/sakit/cuti/dinas luar tidak dapat digunakan. Silahkan matikan fake gps / mock gps nya terlebih dahulu untuk melanjutkan."
}
