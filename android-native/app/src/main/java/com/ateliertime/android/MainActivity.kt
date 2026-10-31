package com.ateliertime.android

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.ateliertime.domain.NotificationExitPolicy
import kotlinx.coroutines.delay

private const val APPROVED_SPLASH_ASSET = "atelier_splash_approved_d139.png"
private const val BRAND_SPLASH_DISPLAY_MS = 900L
private val SPLASH_PARCHMENT = Color(0xFFC8A77C)

// D-139 keeps Android's short required system splash separate from this
// full-frame approved artwork. This image is static, not an animation.
@Composable
private fun AtelierBrandedOpeningScreen() {
    val context = LocalContext.current
    val artwork = remember(context) {
        // The local Samsung build gate packages this exact SHA-checked artwork.
        runCatching {
            context.assets.open(APPROVED_SPLASH_ASSET).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
    }
    Box(modifier = Modifier.fillMaxSize().background(SPLASH_PARCHMENT)) {
        if (artwork != null) {
            Image(
                bitmap = artwork,
                contentDescription = "Atelier Time vintage Vitruvian clock opening artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android root Back means exit, not "move task to background".
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })
        // Preserve the D-138 notification/task lifecycle and saved classes.
        ClassNotificationTaskService.allowNewTask()
        if (savedInstanceState == null) {
            ClassNotificationTaskService.dismiss(applicationContext)
        }
        val coldLaunch = savedInstanceState == null
        setContent {
            var showOpening by remember { mutableStateOf(coldLaunch) }
            LaunchedEffect(showOpening) {
                if (showOpening) {
                    delay(BRAND_SPLASH_DISPLAY_MS)
                    showOpening = false
                }
            }
            if (showOpening) {
                AtelierBrandedOpeningScreen()
            } else {
                MaterialTheme {
                    Surface {
                        SessionEditorScreen()
                    }
                }
            }
        }
    }

    override fun onStop() {
        if (NotificationExitPolicy.shouldDismissOnActivityStop(isFinishing, isChangingConfigurations)) {
            ClassNotificationTaskService.closeTask(applicationContext)
        }
        super.onStop()
    }

    override fun onDestroy() {
        if (NotificationExitPolicy.shouldDismissOnActivityStop(isFinishing, isChangingConfigurations)) {
            ClassNotificationTaskService.closeTask(applicationContext)
        }
        super.onDestroy()
    }
}
