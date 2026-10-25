package com.ateliertime.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // After a genuinely new launch, a previous Recents-swipe must not
        // leave a notification around or block this new Class run.
        ClassNotificationTaskService.allowNewTask()
        if (savedInstanceState == null) {
            ClassNotificationTaskService.dismiss(applicationContext)
        }
        setContent {
            MaterialTheme {
                Surface {
                    SessionEditorScreen()
                }
            }
        }
    }

    override fun onStop() {
        // Back finishes this Activity; Home and Recents overview do not.
        if (isFinishing && !isChangingConfigurations) {
            ClassNotificationTaskService.closeTask(applicationContext)
        }
        super.onStop()
    }

    override fun onDestroy() {
        // Fallback for Back/predictive Back when finish skipped onStop cleanup.
        // Do not treat a rotation or other configuration change as an exit.
        if (isFinishing && !isChangingConfigurations) {
            ClassNotificationTaskService.closeTask(applicationContext)
        }
        super.onDestroy()
    }
}
