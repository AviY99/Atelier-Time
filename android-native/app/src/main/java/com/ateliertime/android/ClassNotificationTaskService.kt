package com.ateliertime.android

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.ateliertime.domain.TimerNotificationStatus

/**
 * D-108: notification-only foreground service; the Compose screen still owns
 * the timer and all A/B/C audio. A service receives onTaskRemoved when Android
 * Recents removes the task, even if Activity.onDestroy is skipped.
 *
 * This service never restarts itself, never recreates a Class and never plays
 * sounds. An Android 14+ specialUse declaration is reviewed at Play release.
 */
class ClassNotificationTaskService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != ACTION_START || taskClosed) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Atelier Time · Running"
        val detail = intent.getStringExtra(EXTRA_DETAIL) ?: "Class in progress"
        val openApp = PendingIntent.getActivity(
            this, 108,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(this, ActiveClassStatusNotification.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_atelier_class_status)
            .setContentTitle(title)
            .setContentText(detail)
            .setContentIntent(openApp)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_STATUS)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .build()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    ActiveClassStatusNotification.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
            } else {
                startForeground(ActiveClassStatusNotification.NOTIFICATION_ID, notification)
            }
        } catch (_: RuntimeException) {
            // This optional status service must never stop Class timers/audio.
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Swipe-away explicitly closes Atelier Time on the user's device.
        // Do not leave the last notification with a frozen countdown.
        taskClosed = true
        removeNotification()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        removeNotification()
        super.onDestroy()
    }

    private fun removeNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(ActiveClassStatusNotification.NOTIFICATION_ID)
    }

    companion object {
        private const val ACTION_START = "com.ateliertime.android.CLASS_NOTIFICATION_START"
        private const val EXTRA_TITLE = "notification_title"
        private const val EXTRA_DETAIL = "notification_detail"

        @Volatile private var taskClosed = false

        fun isTaskClosed(): Boolean = taskClosed

        /** A newly opened Activity is a new Android task, not a stale swipe. */
        fun allowNewTask() { taskClosed = false }

        /** Called only for genuine Back/task finish, not Home or rotation. */
        fun closeTask(context: Context) {
            taskClosed = true
            dismiss(context)
        }

        fun dismiss(context: Context) {
            context.stopService(Intent(context, ClassNotificationTaskService::class.java))
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(ActiveClassStatusNotification.NOTIFICATION_ID)
        }

        fun start(context: Context, snapshot: TimerNotificationStatus): Boolean {
            if (taskClosed) return false
            return try {
                context.startForegroundService(
                    Intent(context, ClassNotificationTaskService::class.java).apply {
                        action = ACTION_START
                        putExtra(EXTRA_TITLE, snapshot.title)
                        putExtra(EXTRA_DETAIL, snapshot.summary)
                    },
                )
                true
            } catch (_: RuntimeException) {
                // A platform-specific FGS restriction cannot prevent a Class.
                false
            }
        }
    }
}
