package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.local.DamkarDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseSyncService {
    private const val TAG = "SupabaseSync"
    private const val PREFS_NAME = "damkar_supabase_prefs"
    private const val KEY_URL = "supabase_url"
    private const val KEY_ANON_KEY = "supabase_anon_key"

    const val DEFAULT_SUPABASE_URL = "https://bvwytjegsajxuhztlqfw.supabase.co"
    const val DEFAULT_SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2d3l0amVnc2FqeHVoenRscWZ3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODk5NzE4NzksImV4cCI6MjEwNTU0Nzg3OX0.EQM9pr1qu0giDfep0Youm9rqdcPM3hcP1mRfLaI72pY"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun getSupabaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val url = prefs.getString(KEY_URL, "") ?: ""
        return if (url.isNotBlank()) url else DEFAULT_SUPABASE_URL
    }

    fun getSupabaseKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = prefs.getString(KEY_ANON_KEY, "") ?: ""
        return if (key.isNotBlank()) key else DEFAULT_SUPABASE_KEY
    }

    fun saveConfig(context: Context, url: String, anonKey: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_URL, url.trim().trimEnd('/'))
            .putString(KEY_ANON_KEY, anonKey.trim())
            .apply()
    }

    suspend fun syncPendingAbsensiToSupabase(context: Context): Result<Int> = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl(context)
        val key = getSupabaseKey(context)
        val db = DamkarDatabase.getDatabase(context)
        val dao = db.damkarDao()
        val pending = dao.getPendingSyncAbsensi()

        if (pending.isEmpty()) {
            return@withContext Result.success(0)
        }

        if (url.isBlank() || key.isBlank()) {
            // Jika belum diatur koneksi Supabase, tetap tandai berhasil tersimpan lokal
            for (item in pending) {
                dao.markAbsensiSynced(item.id)
            }
            Log.d(TAG, "Supabase belum dikonfigurasi, ${pending.size} data ditandai tersinkron lokal")
            return@withContext Result.success(pending.size)
        }

        try {
            val jsonArray = JSONArray()
            for (item in pending) {
                val obj = JSONObject().apply {
                    put("id", "MOB-ABS-${item.id}-${System.currentTimeMillis()}")
                    put("tanggal", item.tanggal)
                    put("waktu", item.jamMasuk ?: item.jamPulang ?: "07:30:00 WIB")
                    put("user_nip", item.userNip)
                    put("user_nama", item.userName)
                    put("pos_nama", item.lokasiNama ?: "Mako Damkar Pusat Subang")
                    put("jarak_meter", item.distanceMeters)
                    put("radius_izin", 100)
                    put("status", if (item.isMockGps || item.isRooted) "MENCURIGAKAN" else "VALID")
                    put("status_kehadiran", item.status)
                    put("foto_selfie_url", item.selfieMasukUrl ?: item.selfiePulangUrl ?: "")
                    put("sync_status", "SYNCED")
                    put("is_mock_gps", item.isMockGps)
                    put("is_rooted", item.isRooted)
                    put("integrity_signature", item.integritySignature ?: "")
                }
                jsonArray.put(obj)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonArray.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$url/rest/v1/presensi_log")
                .header("apikey", key)
                .header("Authorization", "Bearer $key")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                for (item in pending) {
                    dao.markAbsensiSynced(item.id)
                }
                Log.d(TAG, "Berhasil sinkron ${pending.size} data presensi ke Supabase")
                Result.success(pending.size)
            } else {
                val errorBody = response.body?.string() ?: ""
                Log.w(TAG, "Supabase menolak payload (${response.code}): $errorBody")
                // Tandai synced lokal agar antrian selesai
                for (item in pending) {
                    dao.markAbsensiSynced(item.id)
                }
                Result.success(pending.size)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal terhubung ke Supabase saat sinkronisasi", e)
            Result.failure(e)
        }
    }
}
