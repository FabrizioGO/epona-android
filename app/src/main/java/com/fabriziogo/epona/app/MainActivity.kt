package com.fabriziogo.epona.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * The alert a notification tap asked for, or null.
     *
     * Compose state rather than a value read once in [onCreate]: the activity is
     * `singleTop`, so a tap while the app is already running arrives at [onNewIntent] --
     * long after the composition was built around whatever the original intent held.
     */
    private var deepLinkAlertId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No scrim on the status bar in either theme: screens such as the alert detail
        // hero and the map draw their own content all the way up behind it and pick the
        // glyph colour themselves via StatusBarIcons.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )

        deepLinkAlertId = intent?.consumeAlertId()

        setContent {
            EponaTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EponaApp(
                        deepLinkAlertId = deepLinkAlertId,
                        onDeepLinkHandled = { deepLinkAlertId = null },
                        onLaunchGoogleSignIn = { launchGoogleSignIn() },
                        onShareAlert = { text, url -> shareAlert(text, url) },
                        onDialPhone = { phone -> dialPhone(phone) }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Keeps getIntent() in step with what was actually delivered, so a later
        // recreation does not resurrect the intent this activity was started with.
        setIntent(intent)
        intent.consumeAlertId()?.let { deepLinkAlertId = it }
    }

    /**
     * Reads the extra and strips it. Without the strip, every configuration change
     * re-runs onCreate against the same Intent and navigates to the alert all over again.
     */
    private fun Intent.consumeAlertId(): String? =
        getStringExtra(EXTRA_ALERT_ID)?.also { removeExtra(EXTRA_ALERT_ID) }

    private fun launchGoogleSignIn() {
        // TODO: Implement Credential Manager / Google Sign-In
        // 1. Create GoogleSignInRequest via CredentialManager
        // 2. Launch sign-in flow
        // 3. Pass idToken back to AuthViewModel
    }

    private fun shareAlert(text: String, url: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$text\n$url")
            putExtra(Intent.EXTRA_SUBJECT, "Missing Pet Alert — Epona")
        }
        startActivity(Intent.createChooser(shareIntent, "Share alert via"))
    }

    private fun dialPhone(phone: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }
        startActivity(dialIntent)
    }

    companion object {
        const val EXTRA_ALERT_ID = "alert_id"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    }
}
