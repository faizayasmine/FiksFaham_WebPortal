package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FiksFahamApp
import com.example.ui.FiksFahamViewModel
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksFahamTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initFirebaseAppCheckSafely()
    enableEdgeToEdge()
    setContent {
      val viewModel: FiksFahamViewModel = viewModel(
        factory = FiksFahamViewModel.provideFactory(application)
      )
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      val isDark = when (themeMode) {
        com.example.data.preferences.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        com.example.data.preferences.ThemeMode.LIGHT -> false
        com.example.data.preferences.ThemeMode.DARK -> true
      }
      FiksFahamTheme(darkTheme = isDark) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = FiksBg
        ) {
          FiksFahamApp(viewModel = viewModel)
        }
      }
    }
  }

  private fun initFirebaseAppCheckSafely() {
    runCatching {
      if (FirebaseApp.getApps(this).isEmpty()) {
        FirebaseApp.initializeApp(this)
      }
      val appCheck = FirebaseAppCheck.getInstance()
      if (BuildConfig.DEBUG) {
        appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
      } else {
        // Production App Check: Play Integrity / ReCaptcha (diaktifkan bila tersedia di BuildConfig)
        val playIntegrityClass = runCatching {
          Class.forName("com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory")
        }.getOrNull()
        val factoryInstance = playIntegrityClass
          ?.getMethod("getInstance")
          ?.invoke(null) as? com.google.firebase.appcheck.AppCheckProviderFactory
        if (factoryInstance != null) {
          appCheck.installAppCheckProviderFactory(factoryInstance)
        }
      }
    }
  }
}

