package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FiksFaham", appName)
  }

  @Test
  fun `verify auth flow state transitions`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.db.FiksFahamDatabase.getDatabase(application)
    val testRepo = com.example.data.repository.FiksFahamRepository(db.dao(), FakeAuthService())
    val viewModel = com.example.ui.FiksFahamViewModel(application, testRepo)

    // Initial state is SPLASH
    assertEquals(com.example.ui.AuthScreenState.SPLASH, viewModel.authScreenState.value)

    // Complete splash -> ONBOARDING
    viewModel.completeSplash()
    assertEquals(com.example.ui.AuthScreenState.ONBOARDING, viewModel.authScreenState.value)

    // Complete onboarding -> LOGIN
    viewModel.completeOnboarding()
    assertEquals(com.example.ui.AuthScreenState.LOGIN, viewModel.authScreenState.value)

    // Navigate to register -> REGISTER
    viewModel.navigateToRegister()
    assertEquals(com.example.ui.AuthScreenState.REGISTER, viewModel.authScreenState.value)

    // Navigate to forgot password -> FORGOT_PASSWORD
    viewModel.navigateToForgotPassword()
    assertEquals(com.example.ui.AuthScreenState.FORGOT_PASSWORD, viewModel.authScreenState.value)

    // Register with auth -> AUTHENTICATED
    kotlinx.coroutines.runBlocking {
      viewModel.registerWithAuth("Test User", "test@unesa.ac.id", "password123", "UNESA")
    }
    assertEquals(com.example.ui.AuthScreenState.AUTHENTICATED, viewModel.authScreenState.value)

    // Logout -> LOGIN
    viewModel.logout()
    assertEquals(com.example.ui.AuthScreenState.LOGIN, viewModel.authScreenState.value)
  }
}
