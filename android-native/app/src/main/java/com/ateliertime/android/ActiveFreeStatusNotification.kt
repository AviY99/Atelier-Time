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
import com.ateliertime.domain.ActiveTimerNotificationSlots
import com.ateliertime.domain.FreeTimerNotificationFormatter
import com.ateliertime.domain.FreeTimerStatus
import com.ateliertime.domain.FreeTimerView
import com.ateliertime.domain.TimerNotificationStatus

/**
 * D-158: a dedicated, permission-aware Free Timer notification (ID109).
 * Class notification (ID107) is never overwritten by a Free status update.
 * The existing FreeCountdownTimer is the sole monotonic clock/cue owner.
 */
class ActiveFreeStatusNotification(context: Context) {
    private val appContext = context.applicationContext
    private val notifications =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var lastSnapshot: TimerNotificationStatus? = null
    private var lastPostedSecond: Long? = null
    private var posted = false
    private var serviceStarted = false

    init {
        val channel = NotificationChannel(
            CHANNEL_ID, "Free timer status", NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Remaining time for an active independent Free Timer"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        notifications.createNotificationChannel(channel)
    }

    fun update(view: FreeTimerView, nowEpochMs: Long) {
        if (ClassNotificationTaskService.isTaskClosed() ||
            (view.status != FreeTimerStatus.RUNNING &&
                view.status != FreeTimerStatus.PAUSED)) {
            clear()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            appContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            clear()
            return
        }
        if (!notifications.areNotificationsEnabled()) {
            clear()
            return
        }

        val snapshot = FreeTimerNotificationFormatter.forActiveFree(view)
        val nowSecond = nowEpochMs / 1000L
        if (!serviceStarted) {
            // Reuse the existing service: one FGS owns the foreground slot
            // while both Class and Free keep separate notification IDs.
            serviceStarted = ClassNotificationTaskService.startFree(appContext, snapshot)
        }
        if (posted && nowSecond == lastPostedSecond &&
            snapshot.title == lastSnapshot?.title &&
            snapshot.summary == lastSnapshot?.summary) return

        try {
            val openApp = PendingIntent.getActivity(
                appContext, 109,
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
            if (snapshot.countdownMillis != null) {
                notification.setWhen(nowEpochMs + snapshot.countdownMillis)
                    .setShowWhen(true)
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    // Do not timeout a PAUSED timer: it may remain paused indefinitely.
                notification.setTimeoutAfter(
                    view.remainingSeconds.coerceAtLeast(1).toLong() * 1_000L + 2_000L)
            }
            notifications.notify(NOTIFICATION_ID, notification.build())
            posted = true
            lastSnapshot = snapshot
            lastPostedSecond = nowSecond
        } catch (_: RuntimeException) {
            // Optional system UI must never change/freezes a running countdown.
        }
    }

    fun clear() {
        if (posted || serviceStarted) {
            ClassNotificationTaskService.dismissFree(appContext)
        }
        posted = false
        serviceStarted = false
        lastSnapshot = null
        lastPostedSecond = null
    }

    companion object {
        internal const val CHANNEL_ID = "atelier_free_timer_v1"
        internal const val NOTIFICATION_ID = ActiveTimerNotificationSlots.FREE_ID
    }
}
