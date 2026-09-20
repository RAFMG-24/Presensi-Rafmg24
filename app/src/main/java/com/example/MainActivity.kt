package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.model.User
import com.example.data.repository.PresensiRepository
import com.example.ui.screens.admin.*
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.SplashScreen
import com.example.ui.screens.pegawai.*
import com.example.ui.theme.DamkarTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: PresensiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = PresensiRepository(applicationContext)
        lifecycleScope.launch {
            repository.initializeDatabaseIfEmpty()
        }

        setContent {
            DamkarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DamkarAppNavigation(repository = repository)
                }
            }
        }
    }
}

@Composable
fun DamkarAppNavigation(repository: PresensiRepository) {
    var screenStack by remember { mutableStateOf(listOf("splash")) }
    val currentScreen = screenStack.lastOrNull() ?: "login"
    var currentUser by remember { mutableStateOf<User?>(null) }

    fun navigateTo(nextScreen: String) {
        if (screenStack.lastOrNull() != nextScreen) {
            screenStack = screenStack + nextScreen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
            return true
        }
        return false
    }

    fun replaceTop(nextScreen: String) {
        screenStack = listOf(nextScreen)
    }

    // Intercept hardware/system back button
    BackHandler(enabled = screenStack.size > 1 && currentScreen != "login" && currentScreen != "splash") {
        // If we are at the top-level dashboard (admin_dashboard or pegawai_dashboard)
        // pressing back should not go back to splash/login unless explicitly intended;
        // but if previous was dashboard or within sub-menu, go back
        if (currentScreen == "admin_dashboard" || currentScreen == "pegawai_dashboard") {
            // At root dashboard, standard back exits or goes to login if not single-stack
            // Dropping back or allowing system to handle
            val previousScreen = if (screenStack.size >= 2) screenStack[screenStack.size - 2] else null
            if (previousScreen == "login" || previousScreen == "splash") {
                // Do not go back to splash or login from dashboard on back press
            } else {
                navigateBack()
            }
        } else {
            navigateBack()
        }
    }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            "splash" -> {
                SplashScreen(
                    onSplashFinished = {
                        replaceTop("login")
                    }
                )
            }

            "login" -> {
                LoginScreen(
                    repository = repository,
                    onLoginSuccess = { user ->
                        currentUser = user
                        if (user.role == "admin" || user.role == "superadmin") {
                            replaceTop("admin_dashboard")
                        } else {
                            replaceTop("pegawai_dashboard")
                        }
                    }
                )
            }

            // ADMIN ROUTES
            "admin_dashboard" -> {
                if (currentUser != null) {
                    AdminDashboardScreen(
                        adminUser = currentUser!!,
                        repository = repository,
                        onNavigate = { nextRoute -> navigateTo(nextRoute) },
                        onLogout = {
                            currentUser = null
                            replaceTop("login")
                        }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "admin_pegawai" -> {
                if (currentUser != null) {
                    AdminPegawaiScreen(
                        adminUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "admin_unit_kerja" -> {
                if (currentUser != null) {
                    AdminUnitKerjaScreen(
                        adminUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "admin_lokasi" -> {
                if (currentUser != null) {
                    AdminLokasiScreen(
                        adminUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "admin_pengajuan" -> {
                if (currentUser != null) {
                    AdminPengajuanScreen(
                        adminUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "admin_rekap" -> {
                if (currentUser != null) {
                    AdminRekapScreen(
                        adminUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "admin_export" -> {
                if (currentUser != null) {
                    AdminExportScreen(
                        adminUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "admin_audit" -> {
                if (currentUser != null) {
                    AdminAuditLogScreen(
                        adminUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "admin_pengaturan" -> {
                if (currentUser != null) {
                    AdminPengaturanScreen(
                        adminUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "admin_profil" -> {
                if (currentUser != null) {
                    AdminProfilScreen(
                        adminUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() },
                        onLogout = {
                            currentUser = null
                            replaceTop("login")
                        }
                    )
                } else {
                    replaceTop("login")
                }
            }

            // PEGAWAI ROUTES
            "pegawai_dashboard" -> {
                if (currentUser != null) {
                    PegawaiDashboardScreen(
                        pegawaiUser = currentUser!!,
                        repository = repository,
                        onNavigate = { nextRoute -> navigateTo(nextRoute) },
                        onLogout = {
                            currentUser = null
                            replaceTop("login")
                        }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "pegawai_riwayat" -> {
                if (currentUser != null) {
                    PegawaiRiwayatScreen(
                        pegawaiUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "pegawai_pengajuan" -> {
                if (currentUser != null) {
                    PegawaiPengajuanScreen(
                        pegawaiUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() }
                    )
                } else {
                    replaceTop("login")
                }
            }

            "pegawai_profil" -> {
                if (currentUser != null) {
                    PegawaiProfilScreen(
                        pegawaiUser = currentUser!!,
                        repository = repository,
                        onBack = { navigateBack() },
                        onLogout = {
                            currentUser = null
                            replaceTop("login")
                        }
                    )
                } else {
                    replaceTop("login")
                }
            }

            else -> {
                replaceTop("login")
            }
        }
    }
}
