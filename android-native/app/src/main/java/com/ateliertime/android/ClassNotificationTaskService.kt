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
import com.ateliertime.domain.ActiveTimerNotificationSlots

/**
 * D-158: ONE notification-only foreground service for independent Class and Free
 * countdowns. Their clocks, sounds and state remain owned by Compose. While
 * both are active, Class ID107 is foreground and Free ID109 is separately posted.
 * If Class stops first, promote Free ID109 without interrupting its countdown.
 * Do not claim timers survive a killed process: the service never reconstructs
 * any running countdown and remains START_NOT_STICKY.
 *
 * Existing Android 14+ specialUse policy requires Play declaration review.
 */
class ClassNotificationTaskService : Service() {
    private var classActive = false
    private var freeActive = false
    private var foregroundId: Int? = null
    private var classTitle = "Atelier Time · Running"
    private var classDetail = "Class in progress"
    private var freeTitle = "Atelier Time · Free Timer"
    private var freeDetail = "Free Timer running"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (taskClosed) {
            stopSelf()
            return START_NOT_STICKY
        }
        val notifications =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        when (intent?.action) {
            ACTION_CLASS_START -> {
                classActive = true
                classTitle = intent.getStringExtra(EXTRA_TITLE) ?: classTitle
                classDetail = intent.getStringExtra(EXTRA_DETAIL) ?: classDetail
            }
            ACTION_FREE_START -> {
                freeActive = true
                freeTitle = intent.getStringExtra(EXTRA_TITLE) ?: freeTitle
                freeDetail = intent.getStringExtra(EXTRA_DETAIL) ?: freeDetail
            }
            ACTION_CLASS_END -> {
                classActive = false
                notifications.cancel(ActiveClassStatusNotification.NOTIFICATION_ID)
            }
            ACTION_FREE_END -> {
                freeActive = false
                notifications.cancel(ActiveFreeStatusNotification.NOTIFICATION_ID)
            }
            else -> return START_NOT_STICKY
        }
        if (!classActive && !freeActive) {
            stopSelf()
            return START_NOT_STICKY
        }

        val targetId = ActiveTimerNotificationSlots.foregroundId(classActive, freeActive)
            ?: return START_NOT_STICKY
        if (foregroundId != targetId) {
            val previousId = foregroundId
            val title = if (classActive) classTitle else freeTitle
            val detail = if (classActive) classDetail else freeDetail
            val channel = if (classActive) ActiveClassStatusNotification.CHANNEL_ID
                else ActiveFreeStatusNotification.CHANNEL_ID
            val openApp = PendingIntent.getActivity(
                this, 108,
                Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = Notification.Builder(this, channel)
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
                        targetId, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                    )
                } else {
                    startForeground(targetId, notification)
                }
                foregroundId = targetId
                // When the foreground owner switches, the other active timer
                // continues to post its own ID from its independent observer.
                if (previousId != null && previousId != targetId) {
                    if (previousId == ActiveClassStatusNotification.NOTIFICATION_ID
                        && !classActive) notifications.cancel(previousId)
                    if (previousId == ActiveFreeStatusNotification.NOTIFICATION_ID
                        && !freeActive) notifications.cancel(previousId)
                }
            } catch (_: RuntimeException) {
                // Foreground restrictions must not crash either countdown.
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        taskClosed = true
        removeAllNotifications()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        removeAllNotifications()
        super.onDestroy()
    }

    private fun removeAllNotifications() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(ActiveClassStatusNotification.NOTIFICATION_ID)
        manager.cancel(ActiveFreeStatusNotification.NOTIFICATION_ID)
        classActive = false
        freeActive = false
        foregroundId = null
    }

    companion object {
        private const val ACTION_CLASS_START =
            "com.ateliertime.android.CLASS_NOTIFICATION_START"
        private const val ACTION_FREE_START =
            "com.ateliertime.android.FREE_NOTIFICATION_START"
        private const val ACTION_CLASS_END =
            "com.ateliertime.android.CLASS_NOTIFICATION_END"
        private const val ACTION_FREE_END =
            "com.ateliertime.android.FREE_NOTIFICATION_END"
        private const val EXTRA_TITLE = "notification_title"
        private const val EXTRA_DETAIL = "notification_detail"

        @Volatile private var taskClosed = false

        fun isTaskClosed(): Boolean = taskClosed

        /** A newly created Activity starts a new task, not a stale swipe. */
        fun allowNewTask() { taskClosed = false }

        /** Root Back/task finish stops BOTH notifications, not just Class. */
        fun closeTask(context: Context) {
            taskClosed = true
            dismiss(context)
        }

        /** Cold launch: discard orphan notifications without restoring timers. */
        fun dismiss(context: Context) {
            context.stopService(Intent(context, ClassNotificationTaskService::class.java))
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(ActiveClassStatusNotification.NOTIFICATION_ID)
            manager.cancel(ActiveFreeStatusNotification.NOTIFICATION_ID)
        }

        private fun sendEnd(context: Context, action: String, id: Int) {
            if (!taskClosed) {
                try {
                    context.startService(Intent(context, ClassNotificationTaskService::class.java)
                        .apply { this.action = action })
                } catch (_: RuntimeException) {
                    // When Android refuses a background command, still clear
                    // the exact notification without affecting the other ID.
                }
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(id)
        }

        fun dismissClass(context: Context) = sendEnd(
            context, ACTION_CLASS_END, ActiveClassStatusNotification.NOTIFICATION_ID)

        fun dismissFree(context: Context) = sendEnd(
            context, ACTION_FREE_END, ActiveFreeStatusNotification.NOTIFICATION_ID)

        fun start(context: Context, snapshot: TimerNotificationStatus): Boolean =
            startForegroundSlot(context, ACTION_CLASS_START, snapshot)

        fun startFree(context: Context, snapshot: TimerNotificationStatus): Boolean =
            startForegroundSlot(context, ACTION_FREE_START, snapshot)

        private fun startForegroundSlot(
            context: Context, actionName: String, snapshot: TimerNotificationStatus,
        ): Boolean {
            if (taskClosed) return false
            return try {
                context.startForegroundService(
                    Intent(context, ClassNotificationTaskService::class.java).apply {
                        action = actionName
                        putExtra(EXTRA_TITLE, snapshot.title)
                        putExtra(EXTRA_DETAIL, snapshot.summary)
                    },
                )
                true
            } catch (_: RuntimeException) {
                false
            }
        }
    }
}
