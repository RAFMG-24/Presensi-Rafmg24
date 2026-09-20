package com.example.util

import android.content.Context
import android.location.Location
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.MessageDigest

object LocationSecurityUtil {

    // Global mock GPS state (shared between Dashboard, Pengajuan, and other screens)
    private val _isMockGpsActive = MutableStateFlow(false)
    val isMockGpsActive: StateFlow<Boolean> = _isMockGpsActive.asStateFlow()

    fun setMockGpsActive(active: Boolean) {
        _isMockGpsActive.value = active
    }

    /**
     * Checks if a given Android Location is from a mock provider:
     * - location.isMock for API >= 31 (Android 12+)
     * - location.isFromMockProvider for API < 31
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
     * Checks if Developer Options (Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED) is enabled.
     */
    fun isDeveloperOptionsEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) != 0
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks for basic indicators of root / compromised environment (su binary, test-keys, etc.)
     */
    fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    /**
     * Validates device clock integrity using elapsedRealtime to detect backward clock tampering.
     */
    fun isTimeManipulated(recordedLocalTime: Long, lastKnownLocalTime: Long, lastKnownElapsed: Long): Boolean {
        val currentElapsed = SystemClock.elapsedRealtime()
        val elapsedDiff = currentElapsed - lastKnownElapsed
        val timeDiff = recordedLocalTime - lastKnownLocalTime

        // If local time shifted backwards or differs significantly (> 2 minutes) from elapsed clock progression
        if (timeDiff < -5000) return true
        val drift = Math.abs(timeDiff - elapsedDiff)
        return drift > 120_000 // drift > 2 minutes
    }

    /**
     * Computes a cryptographic SHA-256 integrity signature for offline attendance payload
     */
    fun generateOfflineRecordSignature(userId: Long, nip: String, timestamp: Long, lat: Double, lon: Double): String {
        val raw = "$userId|$nip|$timestamp|$lat|$lon|DAMKAR_SUBANG_SALT_2026"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Server-side equivalent Haversine formula calculation.
     */
    fun calculateHaversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    const val MOCK_GPS_WARNING_MESSAGE =
        "Penggunaan Fake GPS terdeteksi. Presensi ditolak! Silakan matikan aplikasi Mock Location / Fake GPS untuk melanjutkan."
}
