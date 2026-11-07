package com.ateliertime.android

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.ateliertime.domain.TimerNotificationFormatter
import com.ateliertime.domain.TimerNotificationStatus
import com.ateliertime.domain.TimerStatus
import com.ateliertime.domain.TimerView

/**
 * D-107 — quiet, permission-aware ongoing Class status notification.
 *
 * The existing Compose Timer remains the sole clock/cue owner. This observer
 * only mirrors snapshots. D-108's small notification-only foreground service
 * handles Recents task removal; it never controls the timer or audio.
 *
 * If the user declined POST_NOTIFICATIONS, the timer works as before.
 */
class ActiveClassStatusNotification(context: Context) {
    private val appContext = context.applicationContext
    private val notifications =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var lastSnapshot: TimerNotificationStatus? = null
    private var lastPostedSecond: Long? = null
    private var posted = false
    private var serviceStarted = false

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Active class status",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Live session and class time remaining while Atelier Time runs"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        notifications.createNotificationChannel(channel)
    }

    fun update(className: String, view: TimerView, classRemainingSeconds: Int, nowEpochMs: Long) {
        if (ClassNotificationTaskService.isTaskClosed()) {
            clear()
            return
        }
        if (view.status != TimerStatus.RUNNING &&
            view.status != TimerStatus.PAUSED &&
            view.status != TimerStatus.STOPPED) {
            clear()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            appContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            // A denied notification permission never prevents a Class from running.
            clear()
            return
        }
        if (!notifications.areNotificationsEnabled()) {
            clear()
            return
        }

        val snapshot = TimerNotificationFormatter.forActiveClass(
            className, view, classRemainingSeconds,
        )
        val nowSecond = nowEpochMs / 1000L
        // Start task-lifecycle monitoring only after the user begins a Class.
        // Android's onTaskRemoved removes this notification after Recents swipe.
        if (!serviceStarted) {
            serviceStarted = ClassNotificationTaskService.start(appContext, snapshot)
        }
        // The underlying clock updates every 200ms, but avoid posting five
        // identical notification updates per second; its chronometer handles ticks.
        if (posted && nowSecond == lastPostedSecond &&
            snapshot.title == lastSnapshot?.title &&
            snapshot.summary == lastSnapshot?.summary) return

        try {
            val openApp = PendingIntent.getActivity(
                appContext,
                107,
                Intent(appContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val notification = Notification.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_atelier_class_status)
                .setContentTitle(snapshot.title)
                .setContentText(snapshot.summary)
                .setStyle(Notification.BigTextStyle().bigText(snapshot.detail))
                .setContentIntent(openApp)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setShowWhen(false)
                // If the process is lost unexpectedly, expire the notification
                // near the original Class deadline instead of leaving it stale.
                .setTimeoutAfter(classRemainingSeconds.coerceAtLeast(1).toLong() *
                    1_000L + 2_000L)

            if (snapshot.countdownMillis != null) {
                notification.setWhen(nowEpochMs + snapshot.countdownMillis)
                    .setShowWhen(true)
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
            }

            notifications.notify(NOTIFICATION_ID, notification.build())
            posted = true
            lastSnapshot = snapshot
            lastPostedSecond = nowSecond
        } catch (_: RuntimeException) {
            // Optional status UI must never crash a running Class, including
            // when Android settings, notification icons or permissions change.
            // Keep previous posted-state so Exit still cancels an older item.
        }
    }

    fun clear() {
        if (posted || serviceStarted) {
            // stopService also removes its foreground notification. Cancel
            // unconditionally: a stale item can survive while this instance
            // has lost its in-memory posted flag.
            ClassNotificationTaskService.dismiss(appContext)
        }
        posted = false
        serviceStarted = false
        lastSnapshot = null
        lastPostedSecond = null
    }

    companion object {
        internal const val CHANNEL_ID = "atelier_active_class_v1"
        internal const val NOTIFICATION_ID = 107
    }
}
