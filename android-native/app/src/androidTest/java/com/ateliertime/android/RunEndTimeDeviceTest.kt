package com.ateliertime.android

import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateliertime.domain.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Side-effect-free device QA. Never changes the saved Class library, audio
 * settings, notifications, permissions, or active app process.
 */
@RunWith(AndroidJUnit4::class)
class RunEndTimeDeviceTest {
    private fun m(n: Int) = 60 * n
    private fun session() = ScheduleEngine().let { engine ->
        engine.approve(engine.generate(SessionRules(
            id = "device-finish", name = "Device finish validation",
            totalSeconds = m(180), summarySeconds = m(15), poseSeconds = m(25),
            specialBreakCount = 1,
        ), FinalPoseChoice.remainder()))
    }

    @Test fun exactPlannedFinishIsValidOnDevice() {
        val s = session()
        val now = System.currentTimeMillis()
        val target = RunEndTimePlanner.suggestedEndEpochMs(s, now)
        val p = RunEndTimePlanner.plan(s, now, target)
        assertEquals(0, p.correctionSeconds)
        assertEquals(s.rules.totalSeconds, p.adjustedDurationSeconds)
    }

    @Test fun shortAndLongSelectedFinishesPreserveSavedApproval() {
        val s = session()
        val saved = s.phases.toList()
        val now = System.currentTimeMillis()
        val before = RunEndTimePlanner.plan(s, now, now + m(177) * 1_000L)
        val after = RunEndTimePlanner.plan(s, now, now + m(186) * 1_000L)
        assertEquals(m(177), before.adjusted.totalSeconds)
        assertEquals(m(186), after.adjusted.totalSeconds)
        assertEquals(saved, s.phases)
    }

    @Test fun impossibleFinishIsRejectedBeforeTimerStarts() {
        val s = session()
        var now = SystemClock.elapsedRealtime()
        val timer = MonotonicSessionTimer(s) { now }
        val chosen = System.currentTimeMillis()
        try {
            RunEndTimePlanner.plan(s, chosen, chosen + m(150) * 1_000L)
            fail("Impossible early finish must be rejected")
        } catch (_: IllegalArgumentException) { }
        assertEquals(TimerStatus.READY, timer.view().status)
    }

    @Test fun monotonicDeadlineSurvivesDisplayedClockShift() {
        val s = session()
        var now = SystemClock.elapsedRealtime()
        val timer = MonotonicSessionTimer(s) { now }
        val started = timer.start(m(3))
        val tracker = ClassClockTracker { now }
        val epoch = System.currentTimeMillis()
        val tracked = tracker.newClass(started, epoch)
        now += m(45) * 1_000L
        val viewed = tracker.observe(timer.update().view, epoch - m(120) * 1_000L)
        assertEquals(m(45), viewed.classElapsedSeconds)
        assertEquals(m(132), viewed.classRemainingSeconds)
        assertEquals(tracked.classEndEpochMs, viewed.classEndEpochMs)
    }

    @Test fun finishIsNeverBeforeOneMinutePerLastSession() {
        val s = session()
        val now = System.currentTimeMillis()
        val lastFeasible = RunEndTimePlanner.plan(s, now, now + m(152) * 1_000L)
        assertEquals(m(1), lastFeasible.adjusted.firstFinalPoseSeconds)
        assertEquals(m(1), lastFeasible.adjusted.lastFinalPoseSeconds)
    }

    @Test fun mainActivityAndForegroundServiceHaveExpectedExportSettings() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES
        )
        val entry = info.activities?.firstOrNull { it.name == MainActivity::class.java.name }
        val foreground = info.services?.firstOrNull {
            it.name == ClassNotificationTaskService::class.java.name
        }
        assertNotNull("MainActivity must be declared", entry)
        assertTrue("Launcher Activity should be exported", entry!!.exported)
        assertNotNull("Foreground service must be declared", foreground)
        assertFalse("Notification service must not be exported", foreground!!.exported)
    }

    @Test fun rejectedClockTextDoesNotChangeLiveTimer() {
        val s = session()
        val timer = MonotonicSessionTimer(s) { SystemClock.elapsedRealtime() }
        try {
            RunEndTimePlanner.nextOccurrence("25:19", System.currentTimeMillis())
            fail("Invalid hours should fail")
        } catch (_: IllegalArgumentException) { }
        assertEquals(TimerStatus.READY, timer.view().status)
    }
}
